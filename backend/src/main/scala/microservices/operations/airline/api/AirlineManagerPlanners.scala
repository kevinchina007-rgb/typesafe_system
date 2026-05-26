package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.persistence.operations.AirlineManagerPlainSql
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection
import java.time.Instant

object RegisterAirlineManagerPlanner extends ConnectionApiPlan[RegisterAirlineManagerPlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "RegisterAirlineManagerPlanner"
  override def plan(input: RegisterAirlineManagerPlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- AirlineManagerPlainSql.registerAirline(connection, input, passwordHash, Instant.now())
    yield response

object ListManagerFlightsPlanner extends ConnectionApiPlan[ManagerFlightsPlannerRequest, ManagerFlightListPlannerResponse]:
  override val name: String = "ListManagerFlightsPlanner"
  override def plan(input: ManagerFlightsPlannerRequest, connection: Connection): IO[ManagerFlightListPlannerResponse] =
    AirlineManagerPlainSql.listFlights(connection, input)

object ListManagerFlightOrdersPlanner extends ConnectionApiPlan[ManagerFlightOrdersPlannerRequest, ManagerFlightOrderListPlannerResponse]:
  override val name: String = "ListManagerFlightOrdersPlanner"
  override def plan(input: ManagerFlightOrdersPlannerRequest, connection: Connection): IO[ManagerFlightOrderListPlannerResponse] =
    AirlineManagerPlainSql.listFlightOrders(connection, input)

object UpdateAirlineManagerProfilePlanner extends ConnectionApiPlan[UpdateAirlineManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateAirlineManagerProfilePlanner"
  override def plan(input: UpdateAirlineManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    AirlineManagerPlainSql.updateAirlineProfile(connection, input, Instant.now())

object CreateManagerFlightPlanner extends ConnectionApiPlan[CreateManagerFlightPlannerRequest, ManagerFlightPlannerResponse]:
  override val name: String = "CreateManagerFlightPlanner"
  override def plan(input: CreateManagerFlightPlannerRequest, connection: Connection): IO[ManagerFlightPlannerResponse] =
    AirlineManagerPlainSql.createFlight(connection, input, Instant.now())

object ToggleManagerFlightStatusPlanner extends ConnectionApiPlan[ToggleManagerFlightStatusPlannerRequest, ManagerFlightPlannerResponse]:
  override val name: String = "ToggleManagerFlightStatusPlanner"
  override def plan(input: ToggleManagerFlightStatusPlannerRequest, connection: Connection): IO[ManagerFlightPlannerResponse] =
    AirlineManagerPlainSql.toggleFlightStatus(connection, input)
