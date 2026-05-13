package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object SignupPlanner extends ConnectionApiPlan[SignupPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "SignupPlanner"
  override def plan(input: SignupPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    val email = EmailAddress.create(input.email).fold(throw _, identity)
    for
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- AuthPlannerPlainSql.signup(connection, input, passwordHash, Instant.now())
    yield response

object LoginPlanner extends ConnectionApiPlan[LoginPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "LoginPlanner"
  override def plan(input: LoginPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    for
      stored <- AuthPlannerPlainSql.login(connection, input, Instant.now())
      (passwordHash, response) = stored
      valid <- verifyPassword(input.password, passwordHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.InvalidPassword(EmailAddress.create(input.email).fold(throw _, identity)))
    yield response

object CurrentUserPlanner extends ConnectionApiPlan[SessionPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "CurrentUserPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())

object LogoutPlanner extends ConnectionApiPlan[SessionPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "LogoutPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    AuthPlannerPlainSql.logout(connection, input.sessionId)

object LogoutOtherSessionsPlanner extends ConnectionApiPlan[LogoutOtherSessionsPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "LogoutOtherSessionsPlanner"
  override def plan(input: LogoutOtherSessionsPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    AuthPlannerPlainSql.logoutOthers(connection, input.sessionId, Instant.now())

object ListAuthSessionsPlanner extends ConnectionApiPlan[SessionPlannerRequest, AuthSessionListPlannerResponse]:
  override val name: String = "ListAuthSessionsPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[AuthSessionListPlannerResponse] =
    AuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())

object ChangePasswordPlanner extends ConnectionApiPlan[ChangePasswordPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "ChangePasswordPlanner"
  override def plan(input: ChangePasswordPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    for
      currentHash <- AuthPlannerPlainSql.passwordHashForSession(connection, input.sessionId)
      valid <- verifyPassword(input.currentPassword, currentHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.CurrentPasswordDidNotMatch)
      newHash <- hashPassword(input.newPassword)
      response <- AuthPlannerPlainSql.changePassword(connection, input.sessionId, newHash, Instant.now())
    yield response
