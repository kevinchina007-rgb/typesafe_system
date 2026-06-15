// 本文件是 auth 域管理员侧 plain SQL 实现，只供后端 planner 编排，不对应前端镜像文件。
package com.typesafe.travel.persistence.auth

import cats.effect.IO
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object ManagerAuthPlannerPlainSql:
  def login(connection: Connection, input: ManagerLoginPlannerRequest, now: Instant): IO[(String, CurrentManagerPlannerResponse)] =
    IO.blocking {
      val managerType = normalizeManagerType(input.managerType)
      val managerTable = tableFor(managerType)
      val scopeColumn = scopeColumnFor(managerType)
      val logoSelect =
        if managerType == "Airline" then "a.logo_asset_path as logo_asset_path"
        else if managerType == "SiteAdmin" then "m.logo_asset_path as logo_asset_path"
        else "null as logo_asset_path"
      val logoJoin = if managerType == "Airline" then "left join airlines a on a.airline_id = m.airline_id" else ""
      val scopeSelect = if managerType == "SiteAdmin" then "'site-admin' as scope_id" else s"m.$scopeColumn as scope_id"
      PlainSqlSupport.withStatement(
        connection,
        s"""
          select c.password_hash, m.manager_id, m.email, m.display_name, m.status, m.created_at, $scopeSelect,
                 $logoSelect
          from manager_credentials c
          join $managerTable m on m.manager_id = c.manager_id
          $logoJoin
          where c.manager_type = ? and c.login_email = ? and c.status = ?
        """
      ) { statement =>
        statement.setString(1, managerType)
        statement.setString(2, input.email.trim)
        statement.setString(3, CredentialStatus.Active.toString)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            val sessionId = s"manager-session-${UUID.randomUUID().toString.take(16)}"
            val expiresAt = now.plus(30, ChronoUnit.DAYS)
            insertManagerSession(connection, sessionId, managerType, resultSet.getString("manager_id"), now, expiresAt)
            resultSet.getString("password_hash") -> readManager(resultSet, sessionId, managerType, expiresAt)
          else throw AuthError.ManagerCredentialWasNotFoundByEmail(authManagerType(managerType), com.typesafe.travel.shared.kernel.EmailAddress.create(input.email).fold(throw _, identity))
        finally resultSet.close()
      }
    }

  def current(connection: Connection, sessionId: String, now: Instant): IO[CurrentManagerPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select s.session_id, s.manager_type, s.expires_at, m.manager_id, m.email, m.display_name, m.status, m.created_at,
                 coalesce(am.airline_id, hm.hotel_id, atm.manager_id) as scope_id,
                 coalesce(aa.logo_asset_path, sam.logo_asset_path) as logo_asset_path
          from auth_sessions s
          left join airline_managers am on s.manager_type = 'Airline' and am.manager_id = s.actor_id
          left join airlines aa on aa.airline_id = am.airline_id
          left join hotel_managers hm on s.manager_type = 'Hotel' and hm.manager_id = s.actor_id
          left join attraction_managers atm on s.manager_type = 'Attraction' and atm.manager_id = s.actor_id
          left join site_admin_managers sam on s.manager_type = 'SiteAdmin' and sam.manager_id = s.actor_id
          left join (
            select manager_id, email, display_name, status, created_at from airline_managers
            union all select manager_id, email, display_name, status, created_at from hotel_managers
            union all select manager_id, email, display_name, status, created_at from attraction_managers
            union all select manager_id, email, display_name, status, created_at from site_admin_managers
          ) m on m.manager_id = s.actor_id
          where s.session_id = ? and s.actor_type = ? and s.status = ? and s.expires_at > ?
        """
      ) { statement =>
        statement.setString(1, sessionId)
        statement.setString(2, AuthActorType.Manager.toString)
        statement.setString(3, AuthSessionStatus.Active.toString)
        statement.setTimestamp(4, Timestamp.from(now))
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then readManager(resultSet, resultSet.getString("session_id"), resultSet.getString("manager_type"), resultSet.getTimestamp("expires_at").toInstant)
          else throw AuthError.ManagerSessionWasRequired
        finally resultSet.close()
      }
    }

  def listSessions(connection: Connection, sessionId: String, now: Instant): IO[ManagerSessionListPlannerResponse] =
    current(connection, sessionId, now).flatMap { manager =>
      IO.blocking {
        PlainSqlSupport.withStatement(
          connection,
          "select session_id, created_at, last_seen_at, expires_at, status from auth_sessions where actor_type = ? and actor_id = ? and manager_type = ? order by created_at desc"
        ) { statement =>
          statement.setString(1, AuthActorType.Manager.toString)
          statement.setString(2, manager.managerId)
          statement.setString(3, manager.managerType)
          val sessions = PlainSqlSupport.queryList(statement) { resultSet =>
            ManagerSessionPlannerResponse(
              sessionId = resultSet.getString("session_id"),
              createdAt = resultSet.getTimestamp("created_at").toInstant,
              lastSeenAt = resultSet.getTimestamp("last_seen_at").toInstant,
              expiresAt = resultSet.getTimestamp("expires_at").toInstant,
              status = resultSet.getString("status"),
              isCurrent = resultSet.getString("session_id") == sessionId
            )
          }
          ManagerSessionListPlannerResponse(sessions)
        }
      }
    }

  def logout(connection: Connection, sessionId: String): IO[ManagerAuthStatusPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update auth_sessions set status = ? where session_id = ?") { statement =>
        statement.setString(1, AuthSessionStatus.Revoked.toString)
        statement.setString(2, sessionId)
        statement.executeUpdate()
      }
      ManagerAuthStatusPlannerResponse("logged_out", None)
    }

  def logoutOthers(connection: Connection, sessionId: String, now: Instant): IO[ManagerAuthStatusPlannerResponse] =
    current(connection, sessionId, now).flatMap { manager =>
      IO.blocking {
        val revoked = PlainSqlSupport.withStatement(
          connection,
          "update auth_sessions set status = ? where actor_type = ? and actor_id = ? and manager_type = ? and session_id <> ? and status = ?"
        ) { statement =>
          statement.setString(1, AuthSessionStatus.Revoked.toString)
          statement.setString(2, AuthActorType.Manager.toString)
          statement.setString(3, manager.managerId)
          statement.setString(4, manager.managerType)
          statement.setString(5, sessionId)
          statement.setString(6, AuthSessionStatus.Active.toString)
          statement.executeUpdate()
        }
        ManagerAuthStatusPlannerResponse("logged_out_others", Some(revoked))
      }
    }

  def passwordHashForSession(connection: Connection, sessionId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        "select c.password_hash from auth_sessions s join manager_credentials c on c.manager_type = s.manager_type and c.manager_id = s.actor_id where s.session_id = ?"
      ) { statement =>
        statement.setString(1, sessionId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("password_hash") else throw AuthError.ManagerSessionWasRequired
        finally resultSet.close()
      }
    }

  def changePassword(connection: Connection, sessionId: String, newPasswordHash: String, now: Instant): IO[ManagerAuthStatusPlannerResponse] =
    current(connection, sessionId, now).flatMap { manager =>
      IO.blocking {
        PlainSqlSupport.withStatement(connection, "update manager_credentials set password_hash = ?, updated_at = ?, password_updated_at = ? where manager_type = ? and manager_id = ?") { statement =>
          statement.setString(1, newPasswordHash)
          statement.setTimestamp(2, Timestamp.from(now))
          statement.setTimestamp(3, Timestamp.from(now))
          statement.setString(4, manager.managerType)
          statement.setString(5, manager.managerId)
          statement.executeUpdate()
        }
        ManagerAuthStatusPlannerResponse("password_changed", None)
      }
    }

  private def insertManagerSession(connection: Connection, sessionId: String, managerType: String, managerId: String, now: Instant, expiresAt: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      "insert into auth_sessions(session_id, actor_type, actor_id, manager_type, created_at, last_seen_at, expires_at, status) values (?, ?, ?, ?, ?, ?, ?, ?)"
    ) { statement =>
      statement.setString(1, sessionId)
      statement.setString(2, AuthActorType.Manager.toString)
      statement.setString(3, managerId)
      statement.setString(4, managerType)
      statement.setTimestamp(5, Timestamp.from(now))
      statement.setTimestamp(6, Timestamp.from(now))
      statement.setTimestamp(7, Timestamp.from(expiresAt))
      statement.setString(8, AuthSessionStatus.Active.toString)
      statement.executeUpdate()
    }

  private def readManager(resultSet: ResultSet, sessionId: String, managerType: String, expiresAt: Instant): CurrentManagerPlannerResponse =
    CurrentManagerPlannerResponse(
      sessionId = sessionId,
      managerId = resultSet.getString("manager_id"),
      managerType = managerType,
      email = resultSet.getString("email"),
      displayName = resultSet.getString("display_name"),
      status = resultSet.getString("status"),
      scopeId = Option(resultSet.getString("scope_id")).getOrElse("site-admin"),
      logoAssetPath = Option(resultSet.getString("logo_asset_path")).map(_.trim).filter(_.nonEmpty),
      createdAt = resultSet.getTimestamp("created_at").toInstant,
      expiresAt = expiresAt
    )

  private def normalizeManagerType(value: String): String =
    value.trim.toLowerCase match
      case "hotel" => "Hotel"
      case "train" => "Train"
      case "attraction" => "Attraction"
      case "siteadmin" | "site-admin" => "SiteAdmin"
      case _ => "Airline"

  private def authManagerType(value: String): AuthManagerType =
    value match
      case "Hotel" => AuthManagerType.Hotel
      case "Train" => AuthManagerType.Train
      case "Attraction" => AuthManagerType.Attraction
      case "SiteAdmin" => AuthManagerType.SiteAdmin
      case _ => AuthManagerType.Airline

  private def tableFor(managerType: String): String =
    managerType match
      case "Hotel" => "hotel_managers"
      case "Attraction" => "attraction_managers"
      case "SiteAdmin" => "site_admin_managers"
      case _ => "airline_managers"

  private def scopeColumnFor(managerType: String): String =
    managerType match
      case "Hotel" => "hotel_id"
      case "Attraction" => "manager_id"
      case "SiteAdmin" => "manager_id"
      case _ => "airline_id"


