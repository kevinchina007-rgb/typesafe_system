// FollowBlogUserPlanner：博客域关注博客用户入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object FollowBlogUserPlanner extends ConnectionApiPlan[BlogUserInteractionPlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "FollowBlogUserPlanner"
  override def plan(input: BlogUserInteractionPlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    val now = Instant.now()
    if input.userId == input.targetUserId then IO.raiseError(new IllegalArgumentException("You cannot follow yourself"))
    else
      BlogPlannerPlainSql.followUser(connection, input.userId, input.targetUserId, now) *>
        BlogPlannerPlainSql.insertNotification(connection, input.targetUserId, Some(input.userId), "userFollowed", None, None, "Someone followed you", now) *>
        BlogPlannerPlainSql.findProfile(connection, BlogProfilePlannerRequest(Some(input.userId), input.targetUserId))
