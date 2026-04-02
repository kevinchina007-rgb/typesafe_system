package com.typesafe.travel.api.memory

import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

private final case class InMemoryAuthState(
    userCredentials: Map[UserId, UserCredential],
    managerCredentials: Map[(AuthManagerType, ManagerId), ManagerCredential],
    sessions: Map[SessionId, AuthSession],
    credentialSequence: Long,
    sessionSequence: Long
)

final class InMemoryAuthRepository[F[_]: Sync] private (stateRef: Ref[F, InMemoryAuthState]) extends AuthRepository[F]:
  override def nextCredentialId: F[CredentialId] =
    stateRef.modify(state => {
      val nextValue = state.credentialSequence + 1
      state.copy(credentialSequence = nextValue) -> CredentialId(s"credential-$nextValue")
    })

  override def nextSessionId: F[SessionId] =
    stateRef.modify(state => {
      val nextValue = state.sessionSequence + 1
      state.copy(sessionSequence = nextValue) -> SessionId(s"session-$nextValue")
    })

  override def findUserCredentialByLoginEmail(loginEmail: EmailAddress): F[Option[UserCredential]] =
    stateRef.get.map(_.userCredentials.values.find(_.loginEmail == loginEmail))

  override def findUserCredentialByUserId(userId: UserId): F[Option[UserCredential]] =
    stateRef.get.map(_.userCredentials.get(userId))

  override def saveUserCredential(userCredential: UserCredential): F[UserCredential] =
    stateRef.update(state => state.copy(userCredentials = state.userCredentials + (userCredential.userId -> userCredential))).as(userCredential)

  override def findManagerCredential(managerType: AuthManagerType, managerId: ManagerId): F[Option[ManagerCredential]] =
    stateRef.get.map(_.managerCredentials.get((managerType, managerId)))

  override def findManagerCredentialByLoginEmail(managerType: AuthManagerType, loginEmail: EmailAddress): F[Option[ManagerCredential]] =
    stateRef.get.map(_.managerCredentials.values.find(credential => credential.managerType == managerType && credential.loginEmail == loginEmail))

  override def saveManagerCredential(managerCredential: ManagerCredential): F[ManagerCredential] =
    stateRef
      .update(state => state.copy(managerCredentials = state.managerCredentials + ((managerCredential.managerType, managerCredential.managerId) -> managerCredential)))
      .as(managerCredential)

  override def findSessionById(sessionId: SessionId): F[Option[AuthSession]] =
    stateRef.get.map(_.sessions.get(sessionId))

  override def saveSession(authSession: AuthSession): F[AuthSession] =
    stateRef.update(state => state.copy(sessions = state.sessions + (authSession.sessionId -> authSession))).as(authSession)

  override def touchSession(sessionId: SessionId, lastSeenAt: Instant, expiresAt: Instant): F[Option[AuthSession]] =
    stateRef.modify { state =>
      val nextSession = state.sessions.get(sessionId).map(_.copy(lastSeenAt = lastSeenAt, expiresAt = expiresAt))
      val nextState = state.copy(sessions = nextSession.fold(state.sessions)(updated => state.sessions + (sessionId -> updated)))
      nextState -> nextSession
    }

  override def expireSession(sessionId: SessionId): F[Unit] =
    updateSessionStatus(sessionId, AuthSessionStatus.Expired)

  override def revokeSession(sessionId: SessionId): F[Unit] =
    updateSessionStatus(sessionId, AuthSessionStatus.Revoked)

  private def updateSessionStatus(sessionId: SessionId, status: AuthSessionStatus): F[Unit] =
    stateRef.update { state =>
      val nextSessions =
        state.sessions.get(sessionId) match
          case Some(existingSession) => state.sessions + (sessionId -> existingSession.copy(status = status))
          case None                  => state.sessions
      state.copy(sessions = nextSessions)
    }

object InMemoryAuthRepository:
  def create[F[_]: Sync]: InMemoryAuthRepository[F] =
    new InMemoryAuthRepository[F](
      Ref.unsafe(
        InMemoryAuthState(
          userCredentials = Map.empty,
          managerCredentials = Map.empty,
          sessions = Map.empty,
          credentialSequence = 100,
          sessionSequence = 100
        )
      )
    )
