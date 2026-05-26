package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.AttractionManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object RegisterAttractionManagerPlanner extends ConnectionApiPlan[RegisterAttractionManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterAttractionManagerPlanner"
  override def plan(input: RegisterAttractionManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- AttractionManagerPlainSql.registerAttraction(connection, input, passwordHash, Instant.now())
    yield response
