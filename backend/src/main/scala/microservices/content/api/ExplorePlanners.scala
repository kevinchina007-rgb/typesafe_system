package com.typesafe.travel.api

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.ExplorePlannerPlainSql

import java.sql.Connection

object ExploreSuggestionsPlanner extends ConnectionApiPlan[ExploreSuggestionsPlannerRequest, ExploreSuggestionListPlannerResponse]:
  override val name: String = "ExploreSuggestionsPlanner"
  override def plan(input: ExploreSuggestionsPlannerRequest, connection: Connection): IO[ExploreSuggestionListPlannerResponse] =
    ExplorePlannerPlainSql.suggestions(connection, input)

object ExploreSearchPlanner extends ConnectionApiPlan[ExploreSearchPlannerRequest, ExploreSearchListPlannerResponse]:
  override val name: String = "ExploreSearchPlanner"
  override def plan(input: ExploreSearchPlannerRequest, connection: Connection): IO[ExploreSearchListPlannerResponse] =
    ExplorePlannerPlainSql.search(connection, input)
