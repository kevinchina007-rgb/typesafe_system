// LikeBlogCommentPlanner：博客域点赞博客评论入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

// 点赞博客评论的业务入口：先落点赞，再决定是否给评论作者发通知。
object LikeBlogCommentPlanner extends ConnectionApiPlan[BlogCommentLikePlannerRequest, BlogPostResponse]:
  override val name: String = "LikeBlogCommentPlanner"

  override def plan(input: BlogCommentLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val now = Instant.now()
    for
      binding <- BlogPlannerPlainSql.findCommentBinding(connection, input.commentId)
      (postId, commentAuthorUserId) = binding
      _ <- BlogPlannerPlainSql.likeComment(connection, input.commentId, input.userId, now)
      _ <-
        if commentAuthorUserId == input.userId then IO.unit
        else BlogPlannerPlainSql.insertNotification(connection, commentAuthorUserId, Some(input.userId), "commentLiked", Some(postId), Some(input.commentId), "有人赞了你的评论", now)
      updated <- BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId))
    yield updated
