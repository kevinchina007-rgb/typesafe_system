// TourGroupMessageActionRoutes 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import com.typesafe.travel.tourgroup.domain.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object TourGroupMessageActionRoutes:
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ PATCH -> Root / "api" / "messages" / messageId =>
      (
        for
          payload <- request.as[EditTourGroupMessagePlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            EditTourGroupMessagePlanner.plan(EditTourGroupMessagePlannerInput(messageId, sessionId, payload.content), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "messages" / messageId / "delete" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            DeleteTourGroupMessagePlanner.plan(DeleteTourGroupMessagePlannerRequest(messageId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "messages" / messageId / "recall" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            RecallTourGroupMessagePlanner.plan(RecallTourGroupMessagePlannerRequest(messageId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "messages" / messageId / "reactions" =>
      (
        for
          payload <- request.as[AddConversationReactionPlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            AddTourGroupMessageReactionPlanner.plan(AddTourGroupMessageReactionPlannerInput(messageId, sessionId, payload.reactionType), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ DELETE -> Root / "api" / "messages" / messageId / "reactions" / reactionType =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            RemoveTourGroupMessageReactionPlanner.plan(RemoveTourGroupMessageReactionPlannerRequest(messageId, sessionId, reactionType), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)
  }
