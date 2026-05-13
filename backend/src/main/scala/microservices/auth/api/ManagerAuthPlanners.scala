package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object ManagerLoginPlanner extends ConnectionApiPlan[ManagerLoginPlannerRequest, CurrentManagerPlannerResponse]:
  override val name: String = "ManagerLoginPlanner"
  override def plan(input: ManagerLoginPlannerRequest, connection: Connection): IO[CurrentManagerPlannerResponse] =
    for
      stored <- ManagerAuthPlannerPlainSql.login(connection, input, Instant.now())
      (passwordHash, response) = stored
      valid <- verifyPassword(input.password, passwordHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.InvalidPassword(EmailAddress.create(input.email).fold(throw _, identity)))
    yield response

object CurrentManagerPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, CurrentManagerPlannerResponse]:
  override val name: String = "CurrentManagerPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[CurrentManagerPlannerResponse] =
    ManagerAuthPlannerPlainSql.current(connection, input.sessionId, Instant.now())

object ManagerLogoutPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerAuthStatusPlannerResponse]:
  override val name: String = "ManagerLogoutPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerAuthStatusPlannerResponse] =
    ManagerAuthPlannerPlainSql.logout(connection, input.sessionId)

object ManagerLogoutOtherSessionsPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerAuthStatusPlannerResponse]:
  override val name: String = "ManagerLogoutOtherSessionsPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerAuthStatusPlannerResponse] =
    ManagerAuthPlannerPlainSql.logoutOthers(connection, input.sessionId, Instant.now())

object ListManagerSessionsPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerAuthSessionListPlannerResponse]:
  override val name: String = "ListManagerSessionsPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerAuthSessionListPlannerResponse] =
    ManagerAuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())

object ChangeManagerPasswordPlanner extends ConnectionApiPlan[ManagerChangePasswordPlannerRequest, ManagerAuthStatusPlannerResponse]:
  override val name: String = "ChangeManagerPasswordPlanner"
  override def plan(input: ManagerChangePasswordPlannerRequest, connection: Connection): IO[ManagerAuthStatusPlannerResponse] =
    for
      currentHash <- ManagerAuthPlannerPlainSql.passwordHashForSession(connection, input.sessionId)
      valid <- verifyPassword(input.currentPassword, currentHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.CurrentPasswordDidNotMatch)
      newHash <- hashPassword(input.newPassword)
      response <- ManagerAuthPlannerPlainSql.changePassword(connection, input.sessionId, newHash, Instant.now())
    yield response
