// PublishBlogPostPlanner 是内容模块的发布入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

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
