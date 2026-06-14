// SearchTourGroupMessagesPlanner 是团体游模块的搜索入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SearchTourGroupMessagesPlanner extends ConnectionApiPlan[SearchTourGroupMessagesPlannerRequest, TourGroupMessageSearchResponse]:

  override val name: String = "SearchTourGroupMessagesPlanner"

  override def plan(input: SearchTourGroupMessagesPlannerRequest, connection: Connection): IO[TourGroupMessageSearchResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.searchMessages(connection, input.groupId, currentUser.userId, input.query, Instant.now())
      result = TourGroupMessageSearchResponse(response)
    yield result
