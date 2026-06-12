// ListAuthSessionsPlanner 是认证模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListAuthSessionsPlanner extends ConnectionApiPlan[SessionPlannerRequest, AuthSessionListPlannerResponse]:
  override val name: String = "ListAuthSessionsPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[AuthSessionListPlannerResponse] =
    AuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())
