// RecallTourGroupMessagePlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object RecallTourGroupMessagePlanner extends ConnectionApiPlan[RecallTourGroupMessagePlannerRequest, TourGroupMessageListPlannerResponse]:

  override val name: String = "RecallTourGroupMessagePlanner"

  override def plan(input: RecallTourGroupMessagePlannerRequest, connection: Connection): IO[TourGroupMessageListPlannerResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.recallMessage(connection, input.messageId, currentUser.userId, Instant.now())
    yield response
