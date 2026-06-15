// UnfavoriteBlogPostPlanner 是内容模块的取消收藏入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object UnfavoriteBlogPostPlanner extends ConnectionApiPlan[BlogFavoritePlannerRequest, BlogPostResponse]:
  override val name: String = "UnfavoriteBlogPostPlanner"
  override def plan(input: BlogFavoritePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.unfavoritePost(connection, input.postId, input.userId) *> BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
