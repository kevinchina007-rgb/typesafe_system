package com.typesafe.travel.operations.domain

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
      response <- TrainManagerPlainSql.registerManager(connection, input, passwordHash, java.time.Instant.now())
    yield response
