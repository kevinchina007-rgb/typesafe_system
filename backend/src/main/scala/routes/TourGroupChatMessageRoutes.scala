// TourGroupChatMessageRoutes 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import com.typesafe.travel.tourgroup.domain.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object TourGroupChatMessageRoutes:
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat" / "search" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          query = request.params.get("q").getOrElse("")
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            SearchTourGroupMessagesPlanner.plan(SearchTourGroupMessagesPlannerRequest(groupId, sessionId, query), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat" / "messages" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            ListTourGroupChatMessagesPlanner.plan(ListTourGroupChatMessagesPlannerRequest(groupId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "tour-groups" / groupId / "chat" / "messages" =>
      (
        for
          payload <- request.as[SendTourGroupMessagePlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            SendTourGroupChatMessagePlanner.plan(SendTourGroupChatMessagePlannerInput(groupId, sessionId, payload), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ GET -> Root / "api" / "conversations" / conversationId / "messages" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            ListTourGroupConversationMessagesPlanner.plan(ListTourGroupConversationMessagesPlannerRequest(conversationId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ GET -> Root / "api" / "direct-conversations" / conversationId / "messages" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            ListTourGroupConversationMessagesPlanner.plan(ListTourGroupConversationMessagesPlannerRequest(conversationId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "conversations" / conversationId / "messages" =>
      (
        for
          payload <- request.as[SendTourGroupMessagePlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            SendTourGroupConversationMessagePlanner.plan(SendTourGroupConversationMessagePlannerInput(conversationId, sessionId, payload), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "direct-conversations" / conversationId / "messages" =>
      (
        for
          payload <- request.as[SendTourGroupMessagePlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            SendTourGroupConversationMessagePlanner.plan(SendTourGroupConversationMessagePlannerInput(conversationId, sessionId, payload), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "conversations" / conversationId / "read" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            MarkTourGroupConversationReadPlanner.plan(MarkTourGroupConversationReadPlannerRequest(conversationId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ POST -> Root / "api" / "conversations" / conversationId / "attachments" =>
      (
        for
          payload <- request.as[UploadConversationAttachmentPlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            UploadTourGroupConversationAttachmentPlanner.plan(
              UploadTourGroupConversationAttachmentPlannerInput(conversationId, sessionId, payload),
              connection
            )
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)
  }
