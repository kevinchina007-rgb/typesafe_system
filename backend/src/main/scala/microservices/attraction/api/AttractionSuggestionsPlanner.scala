// AttractionSuggestionsPlanner 是景点模块的业务入口，负责请求校验、流程编排和结果返回�?
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object AttractionSuggestionsPlanner extends ConnectionApiPlan[AttractionSuggestionRequest, AttractionSuggestionListPlannerResponse]:
  override val name: String = "AttractionSuggestionsPlanner"
  override def plan(input: AttractionSuggestionRequest, connection: Connection): IO[AttractionSuggestionListPlannerResponse] =
    AttractionPlannerPlainSql.suggestions(connection, input)



