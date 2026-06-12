// LoginUserPlanner 是身份模块的登录入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection

object LoginUserPlanner extends ConnectionApiPlan[LoginUserPlannerRequest, UserPlannerResponse]:
  override val name: String = "LoginUserPlanner"

  override def plan(input: LoginUserPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.login(connection, input)
