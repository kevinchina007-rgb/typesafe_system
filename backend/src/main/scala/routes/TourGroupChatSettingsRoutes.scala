// TourGroupChatSettingsRoutes 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import com.typesafe.travel.tourgroup.domain.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object TourGroupChatSettingsRoutes:
  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat-settings" =>
      (
        for
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            LoadTourGroupChatSettingsPlanner.plan(LoadTourGroupChatSettingsPlannerRequest(groupId, sessionId), connection)
          }.flatMap(TourGroupChatResponseSupport.respondJson)
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)

    case request @ PATCH -> Root / "api" / "tour-groups" / groupId / "chat-settings" =>
      (
        for
          payload <- request.as[UpdateTourGroupChatSettingsPlannerRequest]
          sessionId <- TourGroupChatRouterSupport.requireSessionId(request)
          response <- TourGroupChatRouterSupport.withTransactionConnection { connection =>
            UpdateTourGroupChatSettingsPlanner.plan(
              UpdateTourGroupChatSettingsPlannerInput(groupId, sessionId, payload.allowMemberDirectChat),
              connection
            ).flatMap(TourGroupChatResponseSupport.respondJson)
          }
        yield response
      ).handleErrorWith(TourGroupChatResponseSupport.handleRouteError)
  }
