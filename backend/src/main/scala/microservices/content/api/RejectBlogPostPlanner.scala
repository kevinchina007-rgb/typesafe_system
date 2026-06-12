// RejectBlogPostPlanner 是内容模块的拒绝入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object RejectBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "RejectBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Hidden.toString, Instant.now()) *>
      BlogPlannerPlainSql.findPost(connection, input.postId, None)
