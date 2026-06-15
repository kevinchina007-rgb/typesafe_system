// BlogSuggestionsPlanner：博客域博客推荐入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object BlogSuggestionsPlanner extends ConnectionApiPlan[BlogSuggestionRequest, BlogSuggestionListPlannerResponse]:
  override val name: String = "BlogSuggestionsPlanner"
  override def plan(input: BlogSuggestionRequest, connection: Connection): IO[BlogSuggestionListPlannerResponse] =
    BlogPlannerPlainSql.suggestions(connection, input)
