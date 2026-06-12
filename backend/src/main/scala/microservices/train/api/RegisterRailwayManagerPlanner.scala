// RegisterRailwayManagerPlanner 是火车模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection

object RegisterRailwayManagerPlanner extends ConnectionApiPlan[RegisterRailwayManagerPlannerRequest, TrainAdminSessionPlannerResponse]:
  override val name: String = "RegisterRailwayManagerPlanner"
  override def plan(input: RegisterRailwayManagerPlannerRequest, connection: Connection): IO[TrainAdminSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- TrainPlannerPlainSql.registerManager(connection, input, passwordHash, java.time.Instant.now())
    yield response
