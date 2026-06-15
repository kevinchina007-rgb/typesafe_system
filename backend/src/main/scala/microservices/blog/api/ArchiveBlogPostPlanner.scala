// ArchiveBlogPostPlanner：博客域归档博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ArchiveBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPostResponse]:
  override val name: String = "ArchiveBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val userId = input.userId.getOrElse(throw new IllegalArgumentException("userId is required"))
    BlogPlannerPlainSql.findPostAuthor(connection, input.postId).flatMap { authorUserId =>
      if authorUserId != userId then IO.raiseError(new IllegalArgumentException("Only the author can archive this post"))
      else
        BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Hidden.toString, Instant.now()) *>
          BlogPlannerPlainSql.findPost(connection, input.postId, Some(userId))
    }
