// TourGroupConversationRoutes 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import com.typesafe.travel.tourgroup.domain.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object TourGroupConversationRoutes:
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root / "api" / "tour-groups" / groupId / "conversations" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            ListTourGroupConversationsPlanner.plan(ListTourGroupConversationsPlannerRequest(groupId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "direct-conversations" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            ListTourGroupDirectConversationsPlanner.plan(ListTourGroupDirectConversationsPlannerRequest(groupId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat" / "conversations" / "search" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          query = request.params.get("q").getOrElse("")
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            SearchTourGroupConversationsPlanner.plan(SearchTourGroupConversationsPlannerRequest(groupId, sessionId, query), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJsonList)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)
  }
