// 本文件是 auth 域的用户登录入口，只负责请求校验、密码比对和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object LoginPlanner extends ConnectionApiPlan[LoginPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "LoginPlanner"
  override def plan(input: LoginPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    for
      stored <- AuthPlannerPlainSql.login(connection, input, Instant.now())
      (passwordHash, response) = stored
      valid <- verifyPassword(input.password, passwordHash)
      _ <- if valid then IO.unit else IO.raiseError(AuthError.InvalidPassword(EmailAddress.create(input.email).fold(throw _, identity)))
    yield response


