// 本文件是 auth 域的管理员修改密码入口，只负责请求校验、旧密码比对和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

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


