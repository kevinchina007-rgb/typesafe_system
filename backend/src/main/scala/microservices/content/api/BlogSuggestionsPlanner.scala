// BlogSuggestionsPlanner 是内容模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection

object BlogSuggestionsPlanner extends ConnectionApiPlan[BlogSuggestionRequest, BlogSuggestionListPlannerResponse]:
  override val name: String = "BlogSuggestionsPlanner"
  override def plan(input: BlogSuggestionRequest, connection: Connection): IO[BlogSuggestionListPlannerResponse] =
    BlogPlannerPlainSql.suggestions(connection, input)
