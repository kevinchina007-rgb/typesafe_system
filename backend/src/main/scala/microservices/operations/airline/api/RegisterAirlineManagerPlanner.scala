// RegisterAirlineManagerPlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant
import java.util.UUID

object RegisterAirlineManagerPlanner extends ConnectionApiPlan[RegisterAirlineManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterAirlineManagerPlanner"
  override def plan(input: RegisterAirlineManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    val now = Instant.now()
    val airlineId = s"airline-${UUID.randomUUID().toString.take(12)}"
    val managerId = s"manager-${UUID.randomUUID().toString.take(12)}"
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      _ <- validateRegisterAirline(input)
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      _ <- AirlineManagerPlainSql.insertAirline(connection, airlineId, input.airlineName, input.airlineCode, now)
      _ <- AirlineManagerPlainSql.insertAirlineManager(connection, managerId, airlineId, input.email, input.displayName, now)
      _ <- AirlineManagerPlainSql.insertAirlineManagerCredential(connection, managerId, input.email, passwordHash, now)
    yield ManagerSessionPlannerResponse(managerId, "Airline", input.email, input.displayName, "Active", airlineId, None, now.toString)
