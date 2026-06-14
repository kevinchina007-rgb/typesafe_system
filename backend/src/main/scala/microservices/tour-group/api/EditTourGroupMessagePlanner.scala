// EditTourGroupMessagePlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object EditTourGroupMessagePlanner extends ConnectionApiPlan[EditTourGroupMessagePlannerInput, TourGroupMessageListResponse]:

  override val name: String = "EditTourGroupMessagePlanner"

  override def plan(input: EditTourGroupMessagePlannerInput, connection: Connection): IO[TourGroupMessageListResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.editMessage(connection, input.messageId, currentUser.userId, input.content, Instant.now())
    yield response
