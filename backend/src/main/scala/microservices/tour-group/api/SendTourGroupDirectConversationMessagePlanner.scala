package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object SendTourGroupDirectConversationMessagePlanner extends ConnectionApiPlan[SendTourGroupConversationMessagePlannerInput, TourGroupMessageListResponse]:

  override val name: String = "SendTourGroupDirectConversationMessagePlanner"

  override def plan(input: SendTourGroupConversationMessagePlannerInput, connection: Connection): IO[TourGroupMessageListResponse] =
    SendTourGroupConversationMessagePlanner.plan(input, connection)
