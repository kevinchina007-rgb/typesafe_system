// SendTourGroupConversationMessagePlanner 是团体游模块的发送入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SendTourGroupConversationMessagePlanner extends ConnectionApiPlan[SendTourGroupConversationMessagePlannerInput, TourGroupMessageListResponse]:

  override val name: String = "SendTourGroupConversationMessagePlanner"

  override def plan(input: SendTourGroupConversationMessagePlannerInput, connection: Connection): IO[TourGroupMessageListResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.sendMessage(connection, input.conversationId, currentUser.userId, input.payload, Instant.now())
    yield response
