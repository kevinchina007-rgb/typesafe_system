// ListManagerSessionsPlanner 是认证模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListManagerSessionsPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerAuthSessionListPlannerResponse]:
  override val name: String = "ListManagerSessionsPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerAuthSessionListPlannerResponse] =
    ManagerAuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())
