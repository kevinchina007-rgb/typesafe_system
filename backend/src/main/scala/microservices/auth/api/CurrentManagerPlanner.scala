// CurrentManagerPlanner 是认证模块的当前信息查询入口，负责请求校验、流程编排和结果返回。

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
