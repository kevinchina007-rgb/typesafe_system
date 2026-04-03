package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.{TrainError, TrainRepository}

import java.time.Instant
import java.time.temporal.ChronoUnit

final case class CurrentUserSessionView(
    user: User,
    sessionId: SessionId,
    expiresAt: Instant
)

final case class CurrentManagerSessionView(
    managerId: ManagerId,
    managerType: AuthManagerType,
    emailAddress: EmailAddress,
    displayName: PersonName,
    statusLabel: String,
    scopeId: String,
    createdAt: Instant,
    sessionId: SessionId,
    expiresAt: Instant
)

final case class AuthSessionView(
    sessionId: SessionId,
    createdAt: Instant,
    lastSeenAt: Instant,
    expiresAt: Instant,
    status: AuthSessionStatus,
    isCurrent: Boolean
)

trait AuthApplicationService[F[_]]:
  def signupUser(loginEmail: EmailAddress, nickname: PersonName, phone: ContactNumber, rawPassword: String, now: Instant): F[CurrentUserSessionView]
  def loginUser(loginEmail: EmailAddress, rawPassword: String, now: Instant): F[CurrentUserSessionView]
  def restoreCurrentUser(sessionId: SessionId, now: Instant): F[CurrentUserSessionView]
  def listUserSessions(sessionId: SessionId, now: Instant): F[List[AuthSessionView]]
  def changeUserPassword(sessionId: SessionId, currentPassword: String, nextPassword: String, now: Instant): F[Unit]
  def logoutUser(sessionId: SessionId): F[Unit]
  def logoutOtherUserSessions(sessionId: SessionId, now: Instant): F[Int]
  def createManagerCredential(managerType: AuthManagerType, managerId: ManagerId, loginEmail: EmailAddress, rawPassword: String, now: Instant): F[Unit]
  def loginManager(managerType: AuthManagerType, loginEmail: EmailAddress, rawPassword: String, now: Instant): F[CurrentManagerSessionView]
  def restoreCurrentManager(sessionId: SessionId, now: Instant): F[CurrentManagerSessionView]
  def listManagerSessions(sessionId: SessionId, now: Instant): F[List[AuthSessionView]]
  def changeManagerPassword(sessionId: SessionId, currentPassword: String, nextPassword: String, now: Instant): F[Unit]
  def logoutManager(sessionId: SessionId): F[Unit]
  def logoutOtherManagerSessions(sessionId: SessionId, now: Instant): F[Int]

final class LiveAuthApplicationService[F[_]: MonadThrow: Sync](
    authRepository: AuthRepository[F],
    userService: UserService[F],
    userRepository: UserRepository[F],
    managerService: ManagerService[F],
    trainRepository: TrainRepository[F]
) extends AuthApplicationService[F]:
  private val sessionTtlHours = 24L * 14L

  override def signupUser(loginEmail: EmailAddress, nickname: PersonName, phone: ContactNumber, rawPassword: String, now: Instant): F[CurrentUserSessionView] =
    for
      createdUser <- userService.registerUser(loginEmail, nickname, phone, now)
      passwordHash <- hashPasswordForLoginEmail[F](rawPassword, loginEmail)
      credentialId <- authRepository.nextCredentialId
      credential <- MonadThrow[F].fromEither(com.typesafe.travel.auth.domain.createUserCredential(credentialId, createdUser.userId, loginEmail, passwordHash, now))
      _ <- authRepository.saveUserCredential(credential)
      sessionView <- issueUserSession(createdUser, now)
    yield sessionView

  override def loginUser(loginEmail: EmailAddress, rawPassword: String, now: Instant): F[CurrentUserSessionView] =
    for
      credential <- authRepository.findUserCredentialByLoginEmail(loginEmail).flatMap(_.liftTo[F](AuthError.UserCredentialWasNotFoundByEmail(loginEmail)))
      _ <- ensureActiveCredential(credential.loginEmail, credential.status)
      passwordMatches <- verifyPassword[F](rawPassword, credential.passwordHash)
      _ <- if passwordMatches then ().pure[F] else MonadThrow[F].raiseError(AuthError.InvalidPassword(loginEmail))
      user <- userRepository.findByUserId(credential.userId).flatMap(_.liftTo[F](UserError.UserWasNotFound(credential.userId)))
      sessionView <- issueUserSession(user, now)
    yield sessionView

  override def restoreCurrentUser(sessionId: SessionId, now: Instant): F[CurrentUserSessionView] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      userId <- principal match
        case CurrentUserPrincipal(value) => value.pure[F]
        case _                           => MonadThrow[F].raiseError(AuthError.UserSessionWasRequired)
      user <- userRepository.findByUserId(userId).flatMap(_.liftTo[F](UserError.UserWasNotFound(userId)))
      touchedSession <- touchActiveSession(session, now)
    yield CurrentUserSessionView(user, touchedSession.sessionId, touchedSession.expiresAt)

  override def listUserSessions(sessionId: SessionId, now: Instant): F[List[AuthSessionView]] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      userId <- principal match
        case CurrentUserPrincipal(value) => value.pure[F]
        case _                           => MonadThrow[F].raiseError(AuthError.UserSessionWasRequired)
      sessions <- authRepository.listSessions(AuthActorType.User, userId.value, None)
    yield sessions.sortBy(_.createdAt)(Ordering[Instant].reverse).map(toSessionView(_, sessionId))

  override def changeUserPassword(sessionId: SessionId, currentPassword: String, nextPassword: String, now: Instant): F[Unit] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      userId <- principal match
        case CurrentUserPrincipal(value) => value.pure[F]
        case _                           => MonadThrow[F].raiseError(AuthError.UserSessionWasRequired)
      credential <- authRepository.findUserCredentialByUserId(userId).flatMap(_.liftTo[F](AuthError.UserCredentialWasNotFoundByUserId(userId)))
      passwordMatches <- verifyPassword[F](currentPassword, credential.passwordHash)
      _ <- if passwordMatches then ().pure[F] else MonadThrow[F].raiseError(AuthError.CurrentPasswordDidNotMatch)
      nextPasswordHash <- hashPasswordForLoginEmail[F](nextPassword, credential.loginEmail)
      _ <- authRepository.saveUserCredential(credential.copy(passwordHash = nextPasswordHash, updatedAt = now, passwordUpdatedAt = now))
    yield ()

  override def logoutUser(sessionId: SessionId): F[Unit] =
    authRepository.revokeSession(sessionId)

  override def logoutOtherUserSessions(sessionId: SessionId, now: Instant): F[Int] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      userId <- principal match
        case CurrentUserPrincipal(value) => value.pure[F]
        case _                           => MonadThrow[F].raiseError(AuthError.UserSessionWasRequired)
      revokedCount <- authRepository.revokeOtherSessions(sessionId, AuthActorType.User, userId.value, None)
    yield revokedCount

  override def createManagerCredential(
      managerType: AuthManagerType,
      managerId: ManagerId,
      loginEmail: EmailAddress,
      rawPassword: String,
      now: Instant
  ): F[Unit] =
    for
      existingCredential <- authRepository.findManagerCredentialByLoginEmail(managerType, loginEmail)
      _ <- if existingCredential.isDefined then MonadThrow[F].raiseError(AuthError.ManagerCredentialAlreadyExists(managerType, loginEmail)) else ().pure[F]
      passwordHash <- hashPasswordForLoginEmail[F](rawPassword, loginEmail)
      credentialId <- authRepository.nextCredentialId
      managerCredential <- MonadThrow[F].fromEither(com.typesafe.travel.auth.domain.createManagerCredential(credentialId, managerType, managerId, loginEmail, passwordHash, now))
      _ <- authRepository.saveManagerCredential(managerCredential)
    yield ()

  override def loginManager(managerType: AuthManagerType, loginEmail: EmailAddress, rawPassword: String, now: Instant): F[CurrentManagerSessionView] =
    for
      credential <- authRepository.findManagerCredentialByLoginEmail(managerType, loginEmail).flatMap(_.liftTo[F](AuthError.ManagerCredentialWasNotFoundByEmail(managerType, loginEmail)))
      _ <- ensureActiveCredential(credential.loginEmail, credential.status)
      passwordMatches <- verifyPassword[F](rawPassword, credential.passwordHash)
      _ <- if passwordMatches then ().pure[F] else MonadThrow[F].raiseError(AuthError.InvalidPassword(loginEmail))
      sessionView <- issueManagerSession(managerType, credential.managerId, now)
    yield sessionView

  override def restoreCurrentManager(sessionId: SessionId, now: Instant): F[CurrentManagerSessionView] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      currentManagerPrincipal <- principal match
        case managerPrincipal: CurrentManagerPrincipal => managerPrincipal.pure[F]
        case _                                         => MonadThrow[F].raiseError(AuthError.ManagerSessionWasRequired)
      touchedSession <- touchActiveSession(session, now)
      managerView <- loadManagerView(currentManagerPrincipal.managerType, currentManagerPrincipal.managerId)
    yield managerView.copy(sessionId = touchedSession.sessionId, expiresAt = touchedSession.expiresAt)

  override def listManagerSessions(sessionId: SessionId, now: Instant): F[List[AuthSessionView]] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      currentManagerPrincipal <- principal match
        case managerPrincipal: CurrentManagerPrincipal => managerPrincipal.pure[F]
        case _                                         => MonadThrow[F].raiseError(AuthError.ManagerSessionWasRequired)
      sessions <- authRepository.listSessions(
        AuthActorType.Manager,
        currentManagerPrincipal.managerId.value,
        Some(currentManagerPrincipal.managerType)
      )
    yield sessions.sortBy(_.createdAt)(Ordering[Instant].reverse).map(toSessionView(_, sessionId))

  override def changeManagerPassword(sessionId: SessionId, currentPassword: String, nextPassword: String, now: Instant): F[Unit] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      currentManagerPrincipal <- principal match
        case managerPrincipal: CurrentManagerPrincipal => managerPrincipal.pure[F]
        case _                                         => MonadThrow[F].raiseError(AuthError.ManagerSessionWasRequired)
      credential <- authRepository
        .findManagerCredential(currentManagerPrincipal.managerType, currentManagerPrincipal.managerId)
        .flatMap(_.liftTo[F](AuthError.ManagerCredentialWasNotFound(currentManagerPrincipal.managerType, currentManagerPrincipal.managerId)))
      passwordMatches <- verifyPassword[F](currentPassword, credential.passwordHash)
      _ <- if passwordMatches then ().pure[F] else MonadThrow[F].raiseError(AuthError.CurrentPasswordDidNotMatch)
      nextPasswordHash <- hashPasswordForLoginEmail[F](nextPassword, credential.loginEmail)
      _ <- authRepository.saveManagerCredential(credential.copy(passwordHash = nextPasswordHash, updatedAt = now, passwordUpdatedAt = now))
    yield ()

  override def logoutManager(sessionId: SessionId): F[Unit] =
    authRepository.revokeSession(sessionId)

  override def logoutOtherManagerSessions(sessionId: SessionId, now: Instant): F[Int] =
    for
      session <- requireActiveSession(sessionId, now)
      principal <- MonadThrow[F].fromEither(toCurrentPrincipal(session))
      currentManagerPrincipal <- principal match
        case managerPrincipal: CurrentManagerPrincipal => managerPrincipal.pure[F]
        case _                                         => MonadThrow[F].raiseError(AuthError.ManagerSessionWasRequired)
      revokedCount <- authRepository.revokeOtherSessions(
        sessionId,
        AuthActorType.Manager,
        currentManagerPrincipal.managerId.value,
        Some(currentManagerPrincipal.managerType)
      )
    yield revokedCount

  private def ensureActiveCredential(loginEmail: EmailAddress, status: CredentialStatus): F[Unit] =
    status match
      case CredentialStatus.Active   => ().pure[F]
      case CredentialStatus.Disabled => MonadThrow[F].raiseError(AuthError.CredentialWasDisabled(loginEmail))

  private def issueUserSession(user: User, now: Instant): F[CurrentUserSessionView] =
    for
      sessionId <- authRepository.nextSessionId
      expiresAt = now.plus(sessionTtlHours, ChronoUnit.HOURS)
      authSession = createAuthSession(sessionId, CurrentUserPrincipal(user.userId), now, expiresAt)
      _ <- authRepository.saveSession(authSession)
    yield CurrentUserSessionView(user, sessionId, expiresAt)

  private def issueManagerSession(managerType: AuthManagerType, managerId: ManagerId, now: Instant): F[CurrentManagerSessionView] =
    for
      sessionId <- authRepository.nextSessionId
      expiresAt = now.plus(sessionTtlHours, ChronoUnit.HOURS)
      authSession = createAuthSession(sessionId, CurrentManagerPrincipal(managerType, managerId), now, expiresAt)
      _ <- authRepository.saveSession(authSession)
      managerView <- loadManagerView(managerType, managerId)
    yield managerView.copy(sessionId = sessionId, expiresAt = expiresAt)

  private def requireActiveSession(sessionId: SessionId, now: Instant): F[AuthSession] =
    for
      session <- authRepository.findSessionById(sessionId).flatMap(_.liftTo[F](AuthError.SessionWasNotFound(sessionId)))
      _ <- session.status match
        case AuthSessionStatus.Active =>
          if session.expiresAt.isAfter(now) then ().pure[F]
          else authRepository.expireSession(sessionId) *> MonadThrow[F].raiseError(AuthError.SessionWasExpired(sessionId))
        case AuthSessionStatus.Expired => MonadThrow[F].raiseError(AuthError.SessionWasExpired(sessionId))
        case AuthSessionStatus.Revoked => MonadThrow[F].raiseError(AuthError.SessionWasRevoked(sessionId))
    yield session

  private def touchActiveSession(session: AuthSession, now: Instant): F[AuthSession] =
    val nextExpiresAt = now.plus(sessionTtlHours, ChronoUnit.HOURS)
    authRepository.touchSession(session.sessionId, now, nextExpiresAt).map(_.getOrElse(session.copy(lastSeenAt = now, expiresAt = nextExpiresAt)))

  private def toSessionView(session: AuthSession, currentSessionId: SessionId): AuthSessionView =
    AuthSessionView(
      sessionId = session.sessionId,
      createdAt = session.createdAt,
      lastSeenAt = session.lastSeenAt,
      expiresAt = session.expiresAt,
      status = session.status,
      isCurrent = session.sessionId == currentSessionId
    )

  private def loadManagerView(managerType: AuthManagerType, managerId: ManagerId): F[CurrentManagerSessionView] =
    managerType match
      case AuthManagerType.Airline =>
        managerService.loadAirlineManager(managerId).map(airlineManager =>
          CurrentManagerSessionView(
            managerId = airlineManager.managerId,
            managerType = AuthManagerType.Airline,
            emailAddress = airlineManager.primaryEmailAddress,
            displayName = airlineManager.displayName,
            statusLabel = airlineManager.managerStatus.toString,
            scopeId = airlineManager.airlineId.value,
            createdAt = airlineManager.createdAt,
            sessionId = SessionId(""),
            expiresAt = Instant.EPOCH
          )
        )
      case AuthManagerType.Hotel =>
        managerService.loadHotelManager(managerId).map(hotelManager =>
          CurrentManagerSessionView(
            managerId = hotelManager.managerId,
            managerType = AuthManagerType.Hotel,
            emailAddress = hotelManager.primaryEmailAddress,
            displayName = hotelManager.displayName,
            statusLabel = hotelManager.managerStatus.toString,
            scopeId = hotelManager.hotelId.value,
            createdAt = hotelManager.createdAt,
            sessionId = SessionId(""),
            expiresAt = Instant.EPOCH
          )
        )
      case AuthManagerType.Attraction =>
        managerService.loadAttractionManager(managerId).map(attractionManager =>
          CurrentManagerSessionView(
            managerId = attractionManager.managerId,
            managerType = AuthManagerType.Attraction,
            emailAddress = attractionManager.primaryEmailAddress,
            displayName = attractionManager.displayName,
            statusLabel = attractionManager.managerStatus.toString,
            scopeId = attractionManager.managerId.value,
            createdAt = attractionManager.createdAt,
            sessionId = SessionId(""),
            expiresAt = Instant.EPOCH
          )
        )
      case AuthManagerType.Train =>
        trainRepository.findRailwayManagerById(managerId).flatMap(_.liftTo[F](TrainError.RailwayManagerWasNotFoundById(managerId))).map(railwayManager =>
          CurrentManagerSessionView(
            managerId = railwayManager.managerId,
            managerType = AuthManagerType.Train,
            emailAddress = railwayManager.primaryEmailAddress,
            displayName = railwayManager.displayName,
            statusLabel = railwayManager.managerStatus.toString,
            scopeId = railwayManager.operatorCode,
            createdAt = railwayManager.createdAt,
            sessionId = SessionId(""),
            expiresAt = Instant.EPOCH
          )
        )

object LiveAuthApplicationService:
  def apply[F[_]: MonadThrow: Sync](
      authRepository: AuthRepository[F],
      userService: UserService[F],
      userRepository: UserRepository[F],
      managerService: ManagerService[F],
      trainRepository: TrainRepository[F]
  ): LiveAuthApplicationService[F] =
    new LiveAuthApplicationService[F](authRepository, userService, userRepository, managerService, trainRepository)
