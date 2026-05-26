package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*

import java.sql.Connection
import java.time.Instant

object AirlineManagerPlainSql:
  def registerAirline(connection: Connection, input: RegisterAirlineManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerAirline(connection, input, passwordHash, now)

  def listFlights(connection: Connection, input: ManagerFlightsPlannerRequest): IO[ManagerFlightListPlannerResponse] =
    ManagerPlannerPlainSql.listFlights(connection, input)

  def listFlightOrders(connection: Connection, input: ManagerFlightOrdersPlannerRequest): IO[ManagerFlightOrderListPlannerResponse] =
    ManagerPlannerPlainSql.listFlightOrders(connection, input)

  def updateAirlineProfile(connection: Connection, input: UpdateAirlineManagerProfilePlannerRequest, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.updateAirlineProfile(connection, input, now)

  def createFlight(connection: Connection, input: CreateManagerFlightPlannerRequest, now: Instant): IO[ManagerFlightPlannerResponse] =
    ManagerPlannerPlainSql.createFlight(connection, input, now)

  def toggleFlightStatus(connection: Connection, input: ToggleManagerFlightStatusPlannerRequest): IO[ManagerFlightPlannerResponse] =
    ManagerPlannerPlainSql.toggleFlightStatus(connection, input)
