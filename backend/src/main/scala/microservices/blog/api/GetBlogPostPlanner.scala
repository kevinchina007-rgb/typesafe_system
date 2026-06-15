// GetBlogPostPlanner：博客域获取博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object GetBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPostResponse]:
  override val name: String = "GetBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPost(connection, input.postId, input.userId)
