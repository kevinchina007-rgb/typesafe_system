package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum CredentialStatus:
  case Active, Disabled

enum AuthActorType:
  case User, Manager

enum AuthSessionStatus:
  case Active, Expired, Revoked

enum AuthManagerType:
  case Airline, Hotel, Train, Attraction

final case class UserCredential(
    credentialId: CredentialId,
    userId: UserId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant
)

final case class ManagerCredential(
    credentialId: CredentialId,
    managerType: AuthManagerType,
    managerId: ManagerId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant
)

final case class AuthSession(
    sessionId: SessionId,
    actorType: AuthActorType,
    actorId: String,
    managerType: Option[AuthManagerType],
    createdAt: Instant,
    lastSeenAt: Instant,
    expiresAt: Instant,
    status: AuthSessionStatus
)

sealed trait CurrentPrincipal:
  def actorType: AuthActorType

final case class CurrentUserPrincipal(userId: UserId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.User

final case class CurrentManagerPrincipal(managerType: AuthManagerType, managerId: ManagerId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.Manager

trait AuthRepository[F[_]]:
  def nextCredentialId: F[CredentialId]
  def nextSessionId: F[SessionId]
  def findUserCredentialByLoginEmail(loginEmail: EmailAddress): F[Option[UserCredential]]
  def findUserCredentialByUserId(userId: UserId): F[Option[UserCredential]]
  def saveUserCredential(userCredential: UserCredential): F[UserCredential]
  def findManagerCredential(managerType: AuthManagerType, managerId: ManagerId): F[Option[ManagerCredential]]
  def findManagerCredentialByLoginEmail(managerType: AuthManagerType, loginEmail: EmailAddress): F[Option[ManagerCredential]]
  def saveManagerCredential(managerCredential: ManagerCredential): F[ManagerCredential]
  def findSessionById(sessionId: SessionId): F[Option[AuthSession]]
  def saveSession(authSession: AuthSession): F[AuthSession]
  def touchSession(sessionId: SessionId, lastSeenAt: Instant, expiresAt: Instant): F[Option[AuthSession]]
  def expireSession(sessionId: SessionId): F[Unit]
  def revokeSession(sessionId: SessionId): F[Unit]

enum AuthError(val message: String) extends DomainError:
  case PasswordWasEmpty extends AuthError("Password cannot be empty")
  case UserCredentialWasNotFoundByEmail(loginEmail: EmailAddress) extends AuthError(s"User credential '${loginEmail.value}' was not found")
  case ManagerCredentialWasNotFoundByEmail(managerType: AuthManagerType, loginEmail: EmailAddress)
      extends AuthError(s"$managerType credential '${loginEmail.value}' was not found")
  case UserCredentialAlreadyExists(loginEmail: EmailAddress) extends AuthError(s"User credential '${loginEmail.value}' already exists")
  case ManagerCredentialAlreadyExists(managerType: AuthManagerType, loginEmail: EmailAddress)
      extends AuthError(s"$managerType credential '${loginEmail.value}' already exists")
  case CredentialWasDisabled(loginEmail: EmailAddress) extends AuthError(s"Credential '${loginEmail.value}' is disabled")
  case InvalidPassword(loginEmail: EmailAddress) extends AuthError(s"Invalid password for '${loginEmail.value}'")
  case SessionWasNotFound(sessionId: SessionId) extends AuthError(s"Session '${sessionId.value}' was not found")
  case SessionWasExpired(sessionId: SessionId) extends AuthError(s"Session '${sessionId.value}' has expired")
  case SessionWasRevoked(sessionId: SessionId) extends AuthError(s"Session '${sessionId.value}' has been revoked")
  case UserSessionWasRequired extends AuthError("A signed-in user session is required")
  case ManagerSessionWasRequired extends AuthError("A signed-in manager session is required")
  case SessionActorDidNotMatch extends AuthError("The current session does not match the requested actor")
  case ManagerTypeDidNotMatch(expected: AuthManagerType, actual: AuthManagerType)
      extends AuthError(s"Expected manager type $expected but found $actual")
