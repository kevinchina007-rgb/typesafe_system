// UpdateTourGroupDirectConversationArchiveStatePlanner 是团体游模块的更新入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateTourGroupDirectConversationArchiveStatePlanner extends ConnectionApiPlan[UpdateTourGroupDirectConversationArchiveStatePlannerInput, TourGroupConversationSummaryResponse]:

  override val name: String = "UpdateTourGroupDirectConversationArchiveStatePlanner"

  override def plan(input: UpdateTourGroupDirectConversationArchiveStatePlannerInput, connection: Connection): IO[TourGroupConversationSummaryResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.updateArchiveState(connection, input.conversationId, currentUser.userId, input.archived, Instant.now())
    yield response
