package com.typesafe.travel.api.routes

import cats.effect.IO
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object HealthRouter:
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "health" =>
      val backendPort =
        sys.env
          .get("TRAVEL_BACKEND_PORT")
          .flatMap(_.trim.toIntOption)
          .getOrElse(19095)

      Ok(
        Json.obj(
          "status" -> Json.fromString("ok"),
          "service" -> Json.fromString("travel-platform-backend"),
          "backendPort" -> Json.fromInt(backendPort)
        )
      )
  }
