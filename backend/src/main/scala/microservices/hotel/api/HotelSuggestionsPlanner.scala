package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.hotel.tables.HotelSuggestionsPlannerPlainSql

import java.sql.Connection

object HotelSuggestionsPlanner extends ConnectionApiPlan[HotelSuggestionPlannerRequest, SearchSuggestionListPlannerResponse]:
  override val name: String = "HotelSuggestionsPlanner"

  override def plan(input: HotelSuggestionPlannerRequest, connection: Connection): IO[SearchSuggestionListPlannerResponse] =
    HotelSuggestionsPlannerPlainSql.suggestions(connection, input)
