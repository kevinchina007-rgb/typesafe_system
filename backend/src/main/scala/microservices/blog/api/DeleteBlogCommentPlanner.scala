// DeleteBlogCommentPlanner：博客域删除博客评论入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object DeleteBlogCommentPlanner extends ConnectionApiPlan[DeleteBlogCommentPlannerRequest, BlogPostResponse]:
  override val name: String = "DeleteBlogCommentPlanner"
  override def plan(input: DeleteBlogCommentPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findCommentBinding(connection, input.commentId).flatMap { case (postId, authorUserId) =>
      if authorUserId != input.userId then IO.raiseError(new IllegalArgumentException("Only the comment author can delete this comment"))
      else BlogPlannerPlainSql.updateCommentStatus(connection, input.commentId, BlogCommentStatus.Deleted.toString) *> BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId))
    }
