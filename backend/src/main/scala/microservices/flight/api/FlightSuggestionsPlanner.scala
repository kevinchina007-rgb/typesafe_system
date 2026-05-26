package com.typesafe.travel.flight.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.flight.FlightSuggestionsPlannerPlainSql

import java.sql.Connection

object FlightSuggestionsPlanner extends ConnectionApiPlan[FlightSuggestionRequest, SearchSuggestionListPlannerResponse]:
  override val name: String = "FlightSuggestionsPlanner"

  override def plan(input: FlightSuggestionRequest, connection: Connection): IO[SearchSuggestionListPlannerResponse] =
    FlightSuggestionsPlannerPlainSql.suggestFlights(connection, input).map { rows =>
      SearchSuggestionListPlannerResponse(rows.map(toSuggestionResponse))
    }

  private def toSuggestionResponse(row: FlightPlannerRow): SearchSuggestionPlannerResponse =
    SearchSuggestionPlannerResponse(
      resourceType = "flight",
      value = row.flightId,
      title = s"${row.airlineName} ${row.flightNumber}".trim,
      subtitle = s"${row.departureAirport} -> ${row.arrivalAirport}"
    )
