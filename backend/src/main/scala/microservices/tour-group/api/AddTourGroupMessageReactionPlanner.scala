// AddTourGroupMessageReactionPlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object AddTourGroupMessageReactionPlanner extends ConnectionApiPlan[AddTourGroupMessageReactionPlannerInput, TourGroupMessageListPlannerResponse]:

  override val name: String = "AddTourGroupMessageReactionPlanner"

  override def plan(input: AddTourGroupMessageReactionPlannerInput, connection: Connection): IO[TourGroupMessageListPlannerResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      response <- TourGroupChatPlainSql.addReaction(connection, input.messageId, currentUser.userId, input.reactionType, Instant.now())
    yield response
