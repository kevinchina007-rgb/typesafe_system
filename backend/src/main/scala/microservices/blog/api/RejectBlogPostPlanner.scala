// RejectBlogPostPlanner：博客域驳回博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object RejectBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "RejectBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Hidden.toString, Instant.now()) *>
      BlogPlannerPlainSql.findPost(connection, input.postId, None)
