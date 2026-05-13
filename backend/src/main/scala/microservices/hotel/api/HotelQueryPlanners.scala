package com.typesafe.travel.hotel.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.hotel.HotelPlannerPlainSql

import java.sql.Connection

object HotelSuggestionsPlanner extends ConnectionApiPlan[HotelSuggestionRequest, SearchSuggestionListPlannerResponse]:
  override val name: String = "HotelSuggestionsPlanner"

  override def plan(input: HotelSuggestionRequest, connection: Connection): IO[SearchSuggestionListPlannerResponse] =
    HotelPlannerPlainSql.suggestions(connection, input)

object SearchHotelsPlanner extends ConnectionApiPlan[HotelSearchRequest, HotelListPlannerResponse]:
  override val name: String = "SearchHotelsPlanner"

  override def plan(input: HotelSearchRequest, connection: Connection): IO[HotelListPlannerResponse] =
    HotelPlannerPlainSql.list(connection, input)

object GetHotelDetailsPlanner extends ConnectionApiPlan[HotelDetailsRequest, HotelPlannerResponse]:
  override val name: String = "GetHotelDetailsPlanner"

  override def plan(input: HotelDetailsRequest, connection: Connection): IO[HotelPlannerResponse] =
    HotelPlannerPlainSql.details(connection, input)

object BookHotelPlanner extends ConnectionApiPlan[BookHotelPlannerRequest, HotelBookingPlannerResponse]:
  override val name: String = "BookHotelPlanner"

  override def plan(input: BookHotelPlannerRequest, connection: Connection): IO[HotelBookingPlannerResponse] =
    HotelPlannerPlainSql.book(connection, input, java.time.Instant.now())
