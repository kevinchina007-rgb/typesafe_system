package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

trait AuthRepository[F[_]]:
  def nextCredentialId: F[CredentialId]
  def nextSessionId: F[SessionId]
  def findUserCredentialByLoginEmail(loginEmail: EmailAddress): F[Option[UserCredential]]
  def findUserCredentialByUserId(userId: UserId): F[Option[UserCredential]]
  def saveUserCredential(userCredential: UserCredential): F[UserCredential]
  def listSessions(actorType: AuthActorType, actorId: String, managerType: Option[AuthManagerType]): F[List[AuthSession]]
  def findManagerCredential(managerType: AuthManagerType, managerId: ManagerId): F[Option[ManagerCredential]]
  def findManagerCredentialByLoginEmail(managerType: AuthManagerType, loginEmail: EmailAddress): F[Option[ManagerCredential]]
  def saveManagerCredential(managerCredential: ManagerCredential): F[ManagerCredential]
  def findSessionById(sessionId: SessionId): F[Option[AuthSession]]
  def saveSession(authSession: AuthSession): F[AuthSession]
  def touchSession(sessionId: SessionId, lastSeenAt: Instant, expiresAt: Instant): F[Option[AuthSession]]
  def expireSession(sessionId: SessionId): F[Unit]
  def revokeSession(sessionId: SessionId): F[Unit]
  def revokeOtherSessions(currentSessionId: SessionId, actorType: AuthActorType, actorId: String, managerType: Option[AuthManagerType]): F[Int]
