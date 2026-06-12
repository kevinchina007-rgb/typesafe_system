// ManagerLoginPlanner 是认证模块的业务入口，负责请求校验、流程编排和结果返回。

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
