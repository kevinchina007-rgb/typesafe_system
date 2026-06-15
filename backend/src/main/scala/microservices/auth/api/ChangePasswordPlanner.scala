// 本文件是 auth 域的用户修改密码入口，只负责请求校验、旧密码比对和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

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


