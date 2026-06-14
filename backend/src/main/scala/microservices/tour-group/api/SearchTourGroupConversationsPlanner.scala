// SearchTourGroupConversationsPlanner 是团体游模块的搜索入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SearchTourGroupConversationsPlanner extends ConnectionApiPlan[SearchTourGroupConversationsPlannerRequest, List[TourGroupConversationSummaryResponse]]:

  override val name: String = "SearchTourGroupConversationsPlanner"

  override def plan(input: SearchTourGroupConversationsPlannerRequest, connection: Connection): IO[List[TourGroupConversationSummaryResponse]] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.searchConversations(connection, input.groupId, currentUser.userId, input.query, Instant.now())
    yield response
