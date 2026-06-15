// 本文件是 auth 域的登出其他会话入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object LogoutOtherSessionsPlanner extends ConnectionApiPlan[LogoutOtherSessionsPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "LogoutOtherSessionsPlanner"
  override def plan(input: LogoutOtherSessionsPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    AuthPlannerPlainSql.logoutOthers(connection, input.sessionId, Instant.now())


