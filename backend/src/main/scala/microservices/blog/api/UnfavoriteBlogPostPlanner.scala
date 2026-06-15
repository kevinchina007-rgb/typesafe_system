// UnfavoriteBlogPostPlanner：博客域取消收藏博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object UnfavoriteBlogPostPlanner extends ConnectionApiPlan[BlogFavoritePlannerRequest, BlogPostResponse]:
  override val name: String = "UnfavoriteBlogPostPlanner"
  override def plan(input: BlogFavoritePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.unfavoritePost(connection, input.postId, input.userId) *> BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
