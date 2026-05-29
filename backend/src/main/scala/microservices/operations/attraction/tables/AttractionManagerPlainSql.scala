package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.auth.domain.CredentialStatus
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.UUID

object AttractionManagerPlainSql:
  def insertAttractionManager(connection: Connection, managerId: String, email: String, displayName: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into attraction_managers(manager_id, email, display_name, status, created_at) values (?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, managerId)
        statement.setString(2, email.trim)
        statement.setString(3, displayName.trim)
        statement.setString(4, "Active")
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def insertAttractionManagerCredential(connection: Connection, managerId: String, email: String, passwordHash: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into manager_credentials(credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"credential-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, "Attraction")
        statement.setString(3, managerId)
        statement.setString(4, email.trim)
        statement.setString(5, passwordHash)
        statement.setString(6, CredentialStatus.Active.toString)
        statement.setTimestamp(7, Timestamp.from(now))
        statement.setTimestamp(8, Timestamp.from(now))
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
    }
