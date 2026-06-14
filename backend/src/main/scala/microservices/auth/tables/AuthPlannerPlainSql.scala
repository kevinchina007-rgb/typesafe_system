// AuthPlannerPlainSql 灏佽璁よ瘉妯″潡鐨刾lain SQL 瀹炵幇銆?
package com.typesafe.travel.persistence.auth

import cats.effect.IO
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object AuthPlannerPlainSql:
  def signup(connection: Connection, input: SignupPlannerRequest, passwordHash: String, now: Instant): IO[CurrentUserPlannerResponse] =
    IO.blocking {
      val loginEmail = EmailAddress.create(input.email).fold(throw _, identity)
      if userCredentialExists(connection, loginEmail.value) then
        throw AuthError.UserCredentialAlreadyExists(loginEmail)

      val userId = s"user-${UUID.randomUUID().toString.take(12)}"
      val credentialId = s"credential-${UUID.randomUUID().toString.take(12)}"
      val sessionId = s"session-${UUID.randomUUID().toString.take(16)}"
      val expiresAt = now.plus(30, ChronoUnit.DAYS)
      PlainSqlSupport.withStatement(
        connection,
        "insert into users(user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, userId)
        statement.setString(2, input.email.trim)
        statement.setString(3, input.phone.trim)
        statement.setString(4, input.nickname.trim)
        statement.setString(5, null)
        statement.setString(6, "Basic")
        statement.setLong(7, 0L)
        statement.setString(8, "Active")
        statement.setString(9, null)
        statement.setTimestamp(10, Timestamp.from(now))
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(
        connection,
        "insert into user_credentials(credential_id, user_id, login_email, password_hash, status, created_at, updated_at, password_updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, credentialId)
        statement.setString(2, userId)
        statement.setString(3, input.email.trim)
        statement.setString(4, passwordHash)
        statement.setString(5, CredentialStatus.Active.toString)
        statement.setTimestamp(6, Timestamp.from(now))
        statement.setTimestamp(7, Timestamp.from(now))
        statement.setTimestamp(8, Timestamp.from(now))
        statement.executeUpdate()
      }
      insertSession(connection, sessionId, userId, now, expiresAt)
      CurrentUserPlannerResponse(sessionId, userId, input.email.trim, input.nickname.trim, input.phone.trim, None, "Basic", 0L, expiresAt)
    }

  private def userCredentialExists(connection: Connection, email: String): Boolean =
    PlainSqlSupport.withStatement(
      connection,
      "select 1 from user_credentials where login_email = ?"
    ) { statement =>
      statement.setString(1, email.trim)
      val resultSet = statement.executeQuery()
      try resultSet.next()
      finally resultSet.close()
    }

  def login(connection: Connection, input: LoginPlannerRequest, now: Instant): IO[(String, CurrentUserPlannerResponse)] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select u.user_id, u.email, u.phone, u.nickname, u.avatar_url, u.membership_level, u.points, c.password_hash
          from users u
          join user_credentials c on c.user_id = u.user_id
          where c.login_email = ? and c.status = ?
        """
      ) { statement =>
        statement.setString(1, input.email.trim)
        statement.setString(2, CredentialStatus.Active.toString)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            val passwordHash = resultSet.getString("password_hash")
            val sessionId = s"session-${UUID.randomUUID().toString.take(16)}"
            val expiresAt = now.plus(30, ChronoUnit.DAYS)
            val response = readCurrentUser(resultSet, sessionId, expiresAt)
            insertSession(connection, sessionId, response.userId, now, expiresAt)
            passwordHash -> response
          else throw AuthError.UserCredentialWasNotFoundByEmail(EmailAddress.create(input.email).fold(throw _, identity))
        finally resultSet.close()
      }
    }

  def currentUser(connection: Connection, sessionId: String, now: Instant): IO[CurrentUserPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select s.session_id, s.expires_at, u.user_id, u.email, u.phone, u.nickname, u.avatar_url, u.membership_level, u.points
          from auth_sessions s
          join users u on u.user_id = s.actor_id
          where s.session_id = ? and s.actor_type = ? and s.status = ? and s.expires_at > ?
        """
      ) { statement =>
        statement.setString(1, sessionId)
        statement.setString(2, AuthActorType.User.toString)
        statement.setString(3, AuthSessionStatus.Active.toString)
        statement.setTimestamp(4, Timestamp.from(now))
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then readCurrentUser(resultSet, resultSet.getString("session_id"), resultSet.getTimestamp("expires_at").toInstant)
          else throw AuthError.UserSessionWasRequired
        finally resultSet.close()
      }
    }

  def listSessions(connection: Connection, sessionId: String, now: Instant): IO[UserSessionListPlannerResponse] =
    currentUser(connection, sessionId, now).flatMap { current =>
      IO.blocking {
        PlainSqlSupport.withStatement(
          connection,
          "select session_id, created_at, last_seen_at, expires_at, status from auth_sessions where actor_type = ? and actor_id = ? order by created_at desc"
        ) { statement =>
          statement.setString(1, AuthActorType.User.toString)
          statement.setString(2, current.userId)
          PlainSqlSupport.queryList(statement) { resultSet =>
            UserSessionPlannerResponse(
              resultSet.getString("session_id"),
              resultSet.getTimestamp("created_at").toInstant,
              resultSet.getTimestamp("last_seen_at").toInstant,
              resultSet.getTimestamp("expires_at").toInstant,
              resultSet.getString("status")
            )
          }
        }
      }.map(UserSessionListPlannerResponse.apply)
    }

  def logout(connection: Connection, sessionId: String): IO[AuthStatusPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update auth_sessions set status = ? where session_id = ?") { statement =>
        statement.setString(1, AuthSessionStatus.Revoked.toString)
        statement.setString(2, sessionId)
        statement.executeUpdate()
      }
      AuthStatusPlannerResponse("logged_out", None)
    }

  def logoutOthers(connection: Connection, sessionId: String, now: Instant): IO[AuthStatusPlannerResponse] =
    currentUser(connection, sessionId, now).flatMap { current =>
      IO.blocking {
        val revoked = PlainSqlSupport.withStatement(
          connection,
          "update auth_sessions set status = ? where actor_type = ? and actor_id = ? and session_id <> ? and status = ?"
        ) { statement =>
          statement.setString(1, AuthSessionStatus.Revoked.toString)
          statement.setString(2, AuthActorType.User.toString)
          statement.setString(3, current.userId)
          statement.setString(4, sessionId)
          statement.setString(5, AuthSessionStatus.Active.toString)
          statement.executeUpdate()
        }
        AuthStatusPlannerResponse("logged_out_others", Some(revoked))
      }
    }

  def changePassword(connection: Connection, sessionId: String, newPasswordHash: String, now: Instant): IO[AuthStatusPlannerResponse] =
    currentUser(connection, sessionId, now).flatMap { current =>
      IO.blocking {
        PlainSqlSupport.withStatement(connection, "update user_credentials set password_hash = ?, updated_at = ?, password_updated_at = ? where user_id = ?") { statement =>
          statement.setString(1, newPasswordHash)
          statement.setTimestamp(2, Timestamp.from(now))
          statement.setTimestamp(3, Timestamp.from(now))
          statement.setString(4, current.userId)
          statement.executeUpdate()
        }
        AuthStatusPlannerResponse("password_changed", None)
      }
    }

  def passwordHashForSession(connection: Connection, sessionId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        "select c.password_hash from auth_sessions s join user_credentials c on c.user_id = s.actor_id where s.session_id = ?"
      ) { statement =>
        statement.setString(1, sessionId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then resultSet.getString("password_hash")
          else throw AuthError.UserSessionWasRequired
        finally resultSet.close()
      }
    }

  private def insertSession(connection: Connection, sessionId: String, userId: String, now: Instant, expiresAt: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      "insert into auth_sessions(session_id, actor_type, actor_id, manager_type, created_at, last_seen_at, expires_at, status) values (?, ?, ?, ?, ?, ?, ?, ?)"
    ) { statement =>
      statement.setString(1, sessionId)
      statement.setString(2, AuthActorType.User.toString)
      statement.setString(3, userId)
      statement.setString(4, null)
      statement.setTimestamp(5, Timestamp.from(now))
      statement.setTimestamp(6, Timestamp.from(now))
      statement.setTimestamp(7, Timestamp.from(expiresAt))
      statement.setString(8, AuthSessionStatus.Active.toString)
      statement.executeUpdate()
    }

  private def readCurrentUser(resultSet: ResultSet, sessionId: String, expiresAt: Instant): CurrentUserPlannerResponse =
    CurrentUserPlannerResponse(
      sessionId = sessionId,
      userId = resultSet.getString("user_id"),
      email = resultSet.getString("email"),
      nickname = resultSet.getString("nickname"),
      phone = resultSet.getString("phone"),
      avatarUrl = Option(resultSet.getString("avatar_url")),
      membershipLevel = resultSet.getString("membership_level"),
      points = resultSet.getLong("points"),
      expiresAt = expiresAt
    )


