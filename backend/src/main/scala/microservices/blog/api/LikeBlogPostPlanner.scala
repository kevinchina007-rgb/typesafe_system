// LikeBlogPostPlanner：博客域点赞博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object LikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPostResponse]:
  override val name: String = "LikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val now = Instant.now()
    for
      post <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
      _ <- BlogPlannerPlainSql.likePost(connection, input.postId, input.userId, now)
      _ <-
        if post.post.authorUserId == input.userId then IO.unit
        else BlogPlannerPlainSql.insertNotification(connection, post.post.authorUserId, Some(input.userId), "postLiked", Some(input.postId), None, "Someone liked your post", now)
      updated <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
    yield updated
