package com.typesafe.travel.flight.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.flight.FlightPlannerPlainSql

import java.sql.Connection

object FlightSuggestionsPlanner extends ConnectionApiPlan[FlightSuggestionRequest, SearchSuggestionListPlannerResponse]:
  override val name: String = "FlightSuggestionsPlanner"

  override def plan(input: FlightSuggestionRequest, connection: Connection): IO[SearchSuggestionListPlannerResponse] =
    FlightPlannerPlainSql.suggestions(connection, input)

object SearchFlightsPlanner extends ConnectionApiPlan[FlightSearchRequest, FlightListPlannerResponse]:
  override val name: String = "SearchFlightsPlanner"

  override def plan(input: FlightSearchRequest, connection: Connection): IO[FlightListPlannerResponse] =
    FlightPlannerPlainSql.list(connection, input)

object GetFlightDetailsPlanner extends ConnectionApiPlan[FlightDetailsRequest, FlightPlannerResponse]:
  override val name: String = "GetFlightDetailsPlanner"

  override def plan(input: FlightDetailsRequest, connection: Connection): IO[FlightPlannerResponse] =
    FlightPlannerPlainSql.details(connection, input)

object BookFlightPlanner extends ConnectionApiPlan[BookFlightPlannerRequest, FlightBookingPlannerResponse]:
  override val name: String = "BookFlightPlanner"

  override def plan(input: BookFlightPlannerRequest, connection: Connection): IO[FlightBookingPlannerResponse] =
    FlightPlannerPlainSql.book(connection, input, java.time.Instant.now())
