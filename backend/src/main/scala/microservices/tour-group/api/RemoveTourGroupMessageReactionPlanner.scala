// RemoveTourGroupMessageReactionPlanner 是团体游模块的移除入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object RemoveTourGroupMessageReactionPlanner extends ConnectionApiPlan[RemoveTourGroupMessageReactionPlannerRequest, TourGroupMessageListResponse]:

  override val name: String = "RemoveTourGroupMessageReactionPlanner"

  override def plan(input: RemoveTourGroupMessageReactionPlannerRequest, connection: Connection): IO[TourGroupMessageListResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.removeReaction(connection, input.messageId, currentUser.userId, input.reactionType)
    yield response
