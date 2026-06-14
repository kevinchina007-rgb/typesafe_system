// ListTourGroupConversationsPlanner 是团体游模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object ListTourGroupConversationsPlanner extends ConnectionApiPlan[ListTourGroupConversationsPlannerRequest, TourGroupConversationListResponse]:

  override val name: String = "ListTourGroupConversationsPlanner"

  override def plan(input: ListTourGroupConversationsPlannerRequest, connection: Connection): IO[TourGroupConversationListResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.listConversations(connection, input.groupId, currentUser.userId, Instant.now())
    yield response
