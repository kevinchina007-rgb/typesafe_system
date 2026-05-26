package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.SiteAdminManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object RegisterSiteAdminPlanner extends ConnectionApiPlan[RegisterSiteAdminPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterSiteAdminPlanner"
  override def plan(input: RegisterSiteAdminPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- SiteAdminManagerPlainSql.registerSiteAdmin(connection, input, passwordHash, Instant.now())
    yield response
