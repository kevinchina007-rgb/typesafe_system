// PublishBlogPostPlanner：博客域发布博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object PublishBlogPostPlanner extends ConnectionApiPlan[PublishBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "PublishBlogPostPlanner"
  override def plan(input: PublishBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPostAuthor(connection, input.postId).flatMap { authorUserId =>
      if authorUserId != input.userId then IO.raiseError(new IllegalArgumentException("Only the author can publish this draft"))
      else
        BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Published.toString, Instant.now()) *>
          BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
    }
