// HotelSuggestionsPlanner 是酒店模块的业务入口，负责请求校验、流程编排和结果返回。

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
