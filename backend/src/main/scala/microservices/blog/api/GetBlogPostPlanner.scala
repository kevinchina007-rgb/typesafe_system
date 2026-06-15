// GetBlogPostPlanner 是内容模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object GetBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPostResponse]:
  override val name: String = "GetBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPost(connection, input.postId, input.userId)
