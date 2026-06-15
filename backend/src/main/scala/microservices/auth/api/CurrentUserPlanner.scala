// 本文件是 auth 域的当前用户会话入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CurrentUserPlanner extends ConnectionApiPlan[SessionPlannerRequest, CurrentUserPlannerResponse]:
  override val name: String = "CurrentUserPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[CurrentUserPlannerResponse] =
    AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())


