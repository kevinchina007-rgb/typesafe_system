// 本文件定义 CreateUserPlanner，是 identity 模块的创建用户入口，只负责请求校验、流程编排和结果返回。
package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateUserPlanner extends ConnectionApiPlan[CreateUserPlannerRequest, UserPlannerResponse]:
  override val name: String = "CreateUserPlanner"

  override def plan(input: CreateUserPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.create(connection, input, Instant.now())
