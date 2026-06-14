// FlightSuggestionsPlanner 是航班模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*
import com.typesafe.travel.flight.tables.FlightPlannerRow

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.flight.tables.FlightSuggestionsPlannerPlainSql

import java.sql.Connection

object FlightSuggestionsPlanner extends ConnectionApiPlan[FlightSuggestionsPlannerRequest, SearchSuggestionListPlannerResponse]:
  override val name: String = "FlightSuggestionsPlanner"

  override def plan(input: FlightSuggestionsPlannerRequest, connection: Connection): IO[SearchSuggestionListPlannerResponse] =
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
