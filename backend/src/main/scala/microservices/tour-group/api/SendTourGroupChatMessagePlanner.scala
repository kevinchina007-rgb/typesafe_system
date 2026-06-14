// SendTourGroupChatMessagePlanner 是团体游模块的发送入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SendTourGroupChatMessagePlanner extends ConnectionApiPlan[SendTourGroupChatMessagePlannerInput, TourGroupMessageListResponse]:

  override val name: String = "SendTourGroupChatMessagePlanner"

  override def plan(input: SendTourGroupChatMessagePlannerInput, connection: Connection): IO[TourGroupMessageListResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.sendGroupChatMessage(connection, input.groupId, currentUser.userId, input.payload, Instant.now())
    yield response
