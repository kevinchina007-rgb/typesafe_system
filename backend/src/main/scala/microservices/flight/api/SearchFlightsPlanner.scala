package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.flight.tables.SearchFlightsPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SearchFlightsPlanner extends ConnectionApiPlan[FlightSearchPlannerRequest, FlightListPlannerResponse]:
  override val name: String = "SearchFlightsPlanner"

  override def plan(input: FlightSearchPlannerRequest, connection: Connection): IO[FlightListPlannerResponse] =
    for
      rows <- SearchFlightsPlannerPlainSql.searchFlights(connection, input)
      flights <- FlightPlannerResponseBuilder.buildFlightResponses(connection, rows.filter(flightRowIsOpenForBooking), Instant.now())
    yield FlightListPlannerResponse(flights)

