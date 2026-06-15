// UnlikeBlogPostPlanner：博客域取消点赞博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object UnlikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPostResponse]:
  override val name: String = "UnlikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.unlikePost(connection, input.postId, input.userId) *> BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
