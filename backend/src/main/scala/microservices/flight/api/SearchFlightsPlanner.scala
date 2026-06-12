// SearchFlightsPlanner 是航班模块的搜索入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.flight.api

import com.typesafe.travel.flight.objects.*

import cats.effect.IO
import cats.syntax.traverse.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.flight.tables.SearchFlightsPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SearchFlightsPlanner extends ConnectionApiPlan[FlightSearchPlannerRequest, FlightListPlannerResponse]:
  override val name: String = "SearchFlightsPlanner"

  override def plan(input: FlightSearchPlannerRequest, connection: Connection): IO[FlightListPlannerResponse] =
    for
      rows <- SearchFlightsPlannerPlainSql.searchFlights(connection, input)
      now = Instant.now()
      flights <- rows.filter(flightRowIsOpenForBooking).traverse { row =>
        SearchFlightsPlannerPlainSql
          .listCabins(connection, row.flightId)
          .map(cabins => buildFlightPlannerResponse(row, cabins, now))
      }
    yield FlightListPlannerResponse(flights)
