// UnlikeBlogPostPlanner 是内容模块的取消点赞入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object UnlikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPostResponse]:
  override val name: String = "UnlikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.unlikePost(connection, input.postId, input.userId) *> BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
