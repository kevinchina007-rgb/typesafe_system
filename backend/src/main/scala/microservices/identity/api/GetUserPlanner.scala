// 本文件定义 GetUserPlanner，是 identity 模块的获取用户入口，只负责请求校验、流程编排和结果返回。
package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection

object GetUserPlanner extends ConnectionApiPlan[GetUserPlannerRequest, UserPlannerResponse]:
  override val name: String = "GetUserPlanner"

  override def plan(input: GetUserPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.get(connection, input)
