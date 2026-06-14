// DeleteTourGroupMessagePlanner 是团体游模块的删除入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object DeleteTourGroupMessagePlanner extends ConnectionApiPlan[DeleteTourGroupMessagePlannerRequest, TourGroupMessageListResponse]:

  override val name: String = "DeleteTourGroupMessagePlanner"

  override def plan(input: DeleteTourGroupMessagePlannerRequest, connection: Connection): IO[TourGroupMessageListResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.deleteMessage(connection, input.messageId, currentUser.userId, Instant.now())
    yield response
