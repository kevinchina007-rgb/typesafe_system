// UnlikeBlogCommentPlanner：博客域取消点赞博客评论入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

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
