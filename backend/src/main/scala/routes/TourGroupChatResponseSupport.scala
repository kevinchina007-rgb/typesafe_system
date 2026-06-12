// TourGroupChatResponseSupport 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.AuthError
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

object TourGroupChatResponseSupport:
  val logger = Slf4jLogger.getLogger[IO]

  def respondJson[A: Encoder](value: A): IO[Response[IO]] =
    Ok(value.asJson)

  def respondJsonList[A: Encoder](value: List[A]): IO[Response[IO]] =
    Ok(Json.fromValues(value.map(_.asJson)))

  def handleRouteError(error: Throwable): IO[Response[IO]] =
    error match
      case AuthError.UserSessionWasRequired =>
        Response[IO](status = Status.Unauthorized).withEntity(error.getMessage).pure[IO]
      case other =>
        logger.error(other)(s"TourGroupChatRouter failed: ${other.getMessage}") *> BadRequest(Json.obj("error" -> Json.fromString(other.getMessage)))
