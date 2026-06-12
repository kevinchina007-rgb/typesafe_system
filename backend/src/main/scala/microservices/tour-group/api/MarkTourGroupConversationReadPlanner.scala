// MarkTourGroupConversationReadPlanner 是团体游模块的标记入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object MarkTourGroupConversationReadPlanner extends ConnectionApiPlan[MarkTourGroupConversationReadPlannerRequest, TourGroupConversationSummaryPlannerResponse]:

  override val name: String = "MarkTourGroupConversationReadPlanner"

  override def plan(input: MarkTourGroupConversationReadPlannerRequest, connection: Connection): IO[TourGroupConversationSummaryPlannerResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.markConversationRead(connection, input.conversationId, currentUser.userId, Instant.now())
    yield response
