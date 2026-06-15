// 本文件是 auth 域的用户登出入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection

object LogoutPlanner extends ConnectionApiPlan[SessionPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "LogoutPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    AuthPlannerPlainSql.logout(connection, input.sessionId)


