// FavoriteBlogPostPlanner：博客域收藏博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object FavoriteBlogPostPlanner extends ConnectionApiPlan[BlogFavoritePlannerRequest, BlogPostResponse]:
  override val name: String = "FavoriteBlogPostPlanner"
  override def plan(input: BlogFavoritePlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val now = Instant.now()
    for
      post <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
      _ <- BlogPlannerPlainSql.favoritePost(connection, input.postId, input.userId, now)
      _ <-
        if post.post.authorUserId == input.userId then IO.unit
        else BlogPlannerPlainSql.insertNotification(connection, post.post.authorUserId, Some(input.userId), "postFavorited", Some(input.postId), None, "Someone favorited your post", now)
      updated <- BlogPlannerPlainSql.findPost(connection, input.postId, Some(input.userId))
    yield updated
