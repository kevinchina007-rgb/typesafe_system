// 本文件是 auth 域的用户会话列表入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListAuthSessionsPlanner extends ConnectionApiPlan[SessionPlannerRequest, UserSessionListPlannerResponse]:
  override val name: String = "ListAuthSessionsPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[UserSessionListPlannerResponse] =
    AuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())


