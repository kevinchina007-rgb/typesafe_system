// GetOrCreateTourGroupDirectConversationPlanner 是团体游模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object GetOrCreateTourGroupDirectConversationPlanner extends ConnectionApiPlan[GetOrCreateTourGroupDirectConversationPlannerInput, TourGroupConversationSummaryPlannerResponse]:

  override val name: String = "GetOrCreateTourGroupDirectConversationPlanner"

  override def plan(input: GetOrCreateTourGroupDirectConversationPlannerInput, connection: Connection): IO[TourGroupConversationSummaryPlannerResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.getOrCreateDirectConversation(connection, input.groupId, currentUser.userId, input.targetUserId, Instant.now())
    yield response
