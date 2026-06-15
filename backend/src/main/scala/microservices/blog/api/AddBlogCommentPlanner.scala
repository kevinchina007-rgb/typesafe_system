// AddBlogCommentPlanner：博客域新增博客评论入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

// 新增博客评论的业务入口：校验内容、写入评论，并在需要时给作者发通知。
object AddBlogCommentPlanner extends ConnectionApiPlan[BlogCommentPlannerRequest, BlogPostResponse]:
  override val name: String = "AddBlogCommentPlanner"

  override def plan(input: BlogCommentPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val normalizedContent = input.content.trim
    if normalizedContent.isEmpty then IO.raiseError(new IllegalArgumentException("评论内容不能为空。"))
    else
      val now = Instant.now()
      for
        post <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
        commentId <- BlogPlannerPlainSql.insertComment(connection, input.copy(content = normalizedContent), now)
        _ <-
          if post.post.authorUserId == input.userId then IO.unit
          else BlogPlannerPlainSql.insertNotification(connection, post.post.authorUserId, Some(input.userId), "postCommented", Some(input.postId), Some(commentId), s"${post.post.authorDisplayName} 的帖子有了新评论", now)
        updated <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
      yield updated
