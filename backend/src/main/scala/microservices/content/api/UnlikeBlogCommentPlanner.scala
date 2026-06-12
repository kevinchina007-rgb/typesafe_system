// UnlikeBlogCommentPlanner 是内容模块的取消点赞入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection

object UnlikeBlogCommentPlanner extends ConnectionApiPlan[BlogCommentLikePlannerRequest, BlogPostResponse]:
  override val name: String = "UnlikeBlogCommentPlanner"
  override def plan(input: BlogCommentLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    for
      binding <- BlogPlannerPlainSql.findCommentBinding(connection, input.commentId)
      (postId, _) = binding
      _ <- BlogPlannerPlainSql.unlikeComment(connection, input.commentId, input.userId)
      updated <- BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId))
    yield updated
