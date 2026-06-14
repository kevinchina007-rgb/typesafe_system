// AttractionSuggestionsPlanner 是 attraction 模块的建议查询入口，只负责把搜索建议请求转成后端查询和结果返回；前端只需要同名请求/响应对象，不需要复制内部查询实现。
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



