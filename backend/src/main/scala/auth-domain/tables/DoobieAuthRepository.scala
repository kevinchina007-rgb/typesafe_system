package com.typesafe.travel.persistence.auth

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.free.connection.ConnectionIO
import doobie.implicits.*

import java.time.Instant
import java.util.UUID

final class DoobieAuthRepository[F[_]: Async](transactor: Transactor[F]) extends AuthRepository[F]:
  override def nextCredentialId: F[CredentialId] =
    Sync[F].delay(CredentialId(s"credential-${UUID.randomUUID().toString.take(12)}"))

  override def nextSessionId: F[SessionId] =
    Sync[F].delay(SessionId(s"session-${UUID.randomUUID().toString.take(16)}"))

  override def findUserCredentialByLoginEmail(loginEmail: EmailAddress): F[Option[UserCredential]] =
    sql"""
      select credential_id, user_id, login_email, password_hash, status, created_at, updated_at
      from user_credentials
      where login_email = ${loginEmail.value}
    """.query[(String, String, String, String, String, Instant, Instant)].option.transact(transactor).flatMap(_.traverse(toUserCredential))

  override def findUserCredentialByUserId(userId: UserId): F[Option[UserCredential]] =
    sql"""
      select credential_id, user_id, login_email, password_hash, status, created_at, updated_at
      from user_credentials
      where user_id = ${userId.value}
    """.query[(String, String, String, String, String, Instant, Instant)].option.transact(transactor).flatMap(_.traverse(toUserCredential))

  override def saveUserCredential(userCredential: UserCredential): F[UserCredential] =
    (
      sql"""
        update user_credentials
        set login_email = ${userCredential.loginEmail.value},
            password_hash = ${userCredential.passwordHash},
            status = ${userCredential.status.toString},
            updated_at = ${userCredential.updatedAt}
        where user_id = ${userCredential.userId.value}
      """.update.run.flatMap(updatedRows =>
        if updatedRows > 0 then updatedRows.pure[ConnectionIO]
        else
          sql"""
            insert into user_credentials(credential_id, user_id, login_email, password_hash, status, created_at, updated_at)
            values (
              ${userCredential.credentialId.value},
              ${userCredential.userId.value},
              ${userCredential.loginEmail.value},
              ${userCredential.passwordHash},
              ${userCredential.status.toString},
              ${userCredential.createdAt},
              ${userCredential.updatedAt}
            )
          """.update.run
      )
    ).transact(transactor).as(userCredential)

  override def findManagerCredential(managerType: AuthManagerType, managerId: ManagerId): F[Option[ManagerCredential]] =
    sql"""
      select credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at
      from manager_credentials
      where manager_type = ${managerType.toString} and manager_id = ${managerId.value}
    """.query[(String, String, String, String, String, String, Instant, Instant)].option.transact(transactor).flatMap(_.traverse(toManagerCredential))

  override def findManagerCredentialByLoginEmail(managerType: AuthManagerType, loginEmail: EmailAddress): F[Option[ManagerCredential]] =
    sql"""
      select credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at
      from manager_credentials
      where manager_type = ${managerType.toString} and login_email = ${loginEmail.value}
    """.query[(String, String, String, String, String, String, Instant, Instant)].option.transact(transactor).flatMap(_.traverse(toManagerCredential))

  override def saveManagerCredential(managerCredential: ManagerCredential): F[ManagerCredential] =
    (
      sql"""
        update manager_credentials
        set login_email = ${managerCredential.loginEmail.value},
            password_hash = ${managerCredential.passwordHash},
            status = ${managerCredential.status.toString},
            updated_at = ${managerCredential.updatedAt}
        where manager_type = ${managerCredential.managerType.toString}
          and manager_id = ${managerCredential.managerId.value}
      """.update.run.flatMap(updatedRows =>
        if updatedRows > 0 then updatedRows.pure[ConnectionIO]
        else
          sql"""
            insert into manager_credentials(credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at)
            values (
              ${managerCredential.credentialId.value},
              ${managerCredential.managerType.toString},
              ${managerCredential.managerId.value},
              ${managerCredential.loginEmail.value},
              ${managerCredential.passwordHash},
              ${managerCredential.status.toString},
              ${managerCredential.createdAt},
              ${managerCredential.updatedAt}
            )
          """.update.run
      )
    ).transact(transactor).as(managerCredential)

  override def findSessionById(sessionId: SessionId): F[Option[AuthSession]] =
    sql"""
      select session_id, actor_type, actor_id, manager_type, created_at, last_seen_at, expires_at, status
      from auth_sessions
      where session_id = ${sessionId.value}
    """.query[(String, String, String, Option[String], Instant, Instant, Instant, String)].option.transact(transactor).flatMap(_.traverse(toSession))

  override def saveSession(authSession: AuthSession): F[AuthSession] =
    sql"""
      insert into auth_sessions(session_id, actor_type, actor_id, manager_type, created_at, last_seen_at, expires_at, status)
      values (
        ${authSession.sessionId.value},
        ${authSession.actorType.toString},
        ${authSession.actorId},
        ${authSession.managerType.map(_.toString)},
        ${authSession.createdAt},
        ${authSession.lastSeenAt},
        ${authSession.expiresAt},
        ${authSession.status.toString}
      )
    """.update.run.transact(transactor).as(authSession)

  override def touchSession(sessionId: SessionId, lastSeenAt: Instant, expiresAt: Instant): F[Option[AuthSession]] =
    sql"""
      update auth_sessions
      set last_seen_at = $lastSeenAt,
          expires_at = $expiresAt
      where session_id = ${sessionId.value}
    """.update.run.transact(transactor) *> findSessionById(sessionId)

  override def expireSession(sessionId: SessionId): F[Unit] =
    sql"update auth_sessions set status = ${AuthSessionStatus.Expired.toString} where session_id = ${sessionId.value}".update.run.transact(transactor).void

  override def revokeSession(sessionId: SessionId): F[Unit] =
    sql"update auth_sessions set status = ${AuthSessionStatus.Revoked.toString} where session_id = ${sessionId.value}".update.run.transact(transactor).void

  private def toUserCredential(row: (String, String, String, String, String, Instant, Instant)): F[UserCredential] =
    for
      loginEmail <- Async[F].fromEither(EmailAddress.create(row._3))
    yield restorePersistedUserCredential(CredentialId(row._1), UserId(row._2), loginEmail, row._4, CredentialStatus.valueOf(row._5), row._6, row._7)

  private def toManagerCredential(row: (String, String, String, String, String, String, Instant, Instant)): F[ManagerCredential] =
    for
      loginEmail <- Async[F].fromEither(EmailAddress.create(row._4))
    yield restorePersistedManagerCredential(CredentialId(row._1), AuthManagerType.valueOf(row._2), ManagerId(row._3), loginEmail, row._5, CredentialStatus.valueOf(row._6), row._7, row._8)

  private def toSession(row: (String, String, String, Option[String], Instant, Instant, Instant, String)): F[AuthSession] =
    Async[F].pure(
      restorePersistedSession(
        SessionId(row._1),
        AuthActorType.valueOf(row._2),
        row._3,
        row._4.map(AuthManagerType.valueOf),
        row._5,
        row._6,
        row._7,
        AuthSessionStatus.valueOf(row._8)
      )
    )

object DoobieAuthRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieAuthRepository[F] =
    new DoobieAuthRepository[F](transactor)
