// FollowBlogUserPlanner 是内容模块的关注入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

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
