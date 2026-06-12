// BlockBlogUserPlanner 是内容模块的拉黑入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object BlockBlogUserPlanner extends ConnectionApiPlan[BlogUserInteractionPlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "BlockBlogUserPlanner"
  override def plan(input: BlogUserInteractionPlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    if input.userId == input.targetUserId then IO.raiseError(new IllegalArgumentException("You cannot block yourself"))
    else BlogPlannerPlainSql.blockUser(connection, input.userId, input.targetUserId, Instant.now()) *> BlogPlannerPlainSql.findProfile(connection, BlogProfilePlannerRequest(Some(input.userId), input.targetUserId))
