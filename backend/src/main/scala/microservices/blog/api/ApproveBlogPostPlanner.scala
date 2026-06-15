// ApproveBlogPostPlanner 是内容模块的审批入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ApproveBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "ApproveBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.updatePostStatus(connection, input.postId, BlogPostStatus.Published.toString, Instant.now()) *>
      BlogPlannerPlainSql.findPost(connection, input.postId, None)
