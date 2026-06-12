// UpdateTourGroupDirectConversationMuteStatePlanner 是团体游模块的更新入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateTourGroupDirectConversationMuteStatePlanner extends ConnectionApiPlan[UpdateTourGroupDirectConversationMuteStatePlannerInput, TourGroupConversationSummaryPlannerResponse]:

  override val name: String = "UpdateTourGroupDirectConversationMuteStatePlanner"

  override def plan(input: UpdateTourGroupDirectConversationMuteStatePlannerInput, connection: Connection): IO[TourGroupConversationSummaryPlannerResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.updateMuteState(connection, input.conversationId, currentUser.userId, input.muted, Instant.now())
    yield response
