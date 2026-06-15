// 本文件是 auth 域的管理员登出入口，只负责 session 校验和结果返回。
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.ManagerAuthPlannerPlainSql

import java.sql.Connection

object ManagerLogoutPlanner extends ConnectionApiPlan[ManagerSessionPlannerRequest, ManagerAuthStatusPlannerResponse]:
  override val name: String = "ManagerLogoutPlanner"
  override def plan(input: ManagerSessionPlannerRequest, connection: Connection): IO[ManagerAuthStatusPlannerResponse] =
    ManagerAuthPlannerPlainSql.logout(connection, input.sessionId)


