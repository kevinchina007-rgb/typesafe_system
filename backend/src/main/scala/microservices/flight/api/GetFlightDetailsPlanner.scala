package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.flight.tables.GetFlightDetailsPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object GetFlightDetailsPlanner extends ConnectionApiPlan[FlightDetailsRequest, FlightPlannerResponse]:
  override val name: String = "GetFlightDetailsPlanner"

  override def plan(input: FlightDetailsRequest, connection: Connection): IO[FlightPlannerResponse] =
    for
      row <- GetFlightDetailsPlannerPlainSql.findFlight(connection, input.flightId).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(new IllegalArgumentException(s"Flight '${input.flightId}' was not found"))
      }
      cabins <- GetFlightDetailsPlannerPlainSql.listCabins(connection, row.flightId)
    yield buildFlightPlannerResponse(row, cabins, Instant.now())
