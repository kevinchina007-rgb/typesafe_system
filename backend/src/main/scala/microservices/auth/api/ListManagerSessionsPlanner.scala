// 本文件是 auth 域的管理员会话列表入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListManagerSessionsPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerSessionListPlannerResponse]:
  override val name: String = "ListManagerSessionsPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerSessionListPlannerResponse] =
    ManagerAuthPlannerPlainSql.listSessions(connection, input.sessionId, Instant.now())


