// TourGroupDirectConversationRoutes 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import com.typesafe.travel.tourgroup.domain.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object TourGroupDirectConversationRoutes:
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root / "api" / "tour-groups" / groupId / "direct-conversations" =>
      (
        for
          payload <- request.as[GetOrCreateTourGroupDirectConversationPlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            GetOrCreateTourGroupDirectConversationPlanner.plan(
              GetOrCreateTourGroupDirectConversationPlannerInput(groupId, sessionId, payload.targetUserId),
              connection
            )
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationId / "mute" =>
      (
        for
          payload <- request.as[UpdateDirectConversationMuteStatePlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            UpdateTourGroupDirectConversationMuteStatePlanner.plan(
              UpdateTourGroupDirectConversationMuteStatePlannerInput(conversationId, sessionId, payload.muted),
              connection
            )
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationId / "archive" =>
      (
        for
          payload <- request.as[UpdateDirectConversationArchiveStatePlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            UpdateTourGroupDirectConversationArchiveStatePlanner.plan(
              UpdateTourGroupDirectConversationArchiveStatePlannerInput(conversationId, sessionId, payload.archived),
              connection
            )
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)
  }
