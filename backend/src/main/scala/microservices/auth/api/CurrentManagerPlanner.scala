// 本文件是 auth 域的当前管理员会话入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CurrentManagerPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, CurrentManagerPlannerResponse]:
  override val name: String = "CurrentManagerPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[CurrentManagerPlannerResponse] =
    ManagerAuthPlannerPlainSql.current(connection, input.sessionId, Instant.now())


