// SiteAdminManagerPlainSql 负责operations相关实现。

package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.auth.domain.CredentialStatus
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object SiteAdminManagerPlainSql:
  def insertSiteAdminManager(connection: Connection, managerId: String, email: String, displayName: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into site_admin_managers(manager_id, email, display_name, status, created_at) values (?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, managerId)
        statement.setString(2, email.trim)
        statement.setString(3, displayName.trim)
        statement.setString(4, "Active")
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def updateSiteAdminProfile(connection: Connection, managerId: String, displayName: String, logoAssetPath: Option[String]): IO[SiteAdminManagerSessionPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update site_admin_managers set display_name = ?, logo_asset_path = ? where manager_id = ?") { statement =>
        statement.setString(1, displayName.trim)
        statement.setString(2, logoAssetPath.map(_.trim).filter(_.nonEmpty).orNull)
        statement.setString(3, managerId.trim)
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(
        connection,
        "select manager_id, email, display_name, status, created_at, logo_asset_path from site_admin_managers where manager_id = ?"
      ) { statement =>
        statement.setString(1, managerId.trim)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then readSiteAdminSession(resultSet)
          else throw new IllegalArgumentException(s"site admin manager '$managerId' was not found")
        finally resultSet.close()
      }
    }

  def insertSiteAdminCredential(connection: Connection, managerId: String, email: String, passwordHash: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into manager_credentials(credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"credential-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, "SiteAdmin")
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

  private def readSiteAdminSession(resultSet: ResultSet): SiteAdminManagerSessionPlannerResponse =
    SiteAdminManagerSessionPlannerResponse(
      managerId = resultSet.getString("manager_id"),
      managerType = "SiteAdmin",
      email = resultSet.getString("email"),
      displayName = resultSet.getString("display_name"),
      status = resultSet.getString("status"),
      scopeId = "site-admin",
      logoAssetPath = Option(resultSet.getString("logo_asset_path")).map(_.trim).filter(_.nonEmpty),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString
    )
