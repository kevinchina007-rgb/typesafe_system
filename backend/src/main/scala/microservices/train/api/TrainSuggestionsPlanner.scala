// TrainSuggestionsPlanner 是火车模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object TrainSuggestionsPlanner extends ConnectionApiPlan[TrainSuggestionPlannerRequest, TrainSuggestionListPlannerResponse]:
  override val name: String = "TrainSuggestionsPlanner"
  override def plan(input: TrainSuggestionPlannerRequest, connection: Connection): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSql.suggestions(connection, input)
