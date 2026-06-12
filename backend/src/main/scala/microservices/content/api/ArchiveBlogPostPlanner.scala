// ArchiveBlogPostPlanner 是内容模块的归档入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

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
