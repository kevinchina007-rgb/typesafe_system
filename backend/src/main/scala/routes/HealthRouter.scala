package com.typesafe.travel.api.routes

import cats.effect.IO
import com.typesafe.travel.api.dto.HealthResponseDto
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*

object HealthRouter:
  private given Encoder[HealthResponseDto] = deriveEncoder

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "health" =>
      Ok(
        HealthResponseDto(
          status = "ok",
          service = "travel-platform-backend",
          backendPort = sys.env.get("TRAVEL_BACKEND_PORT").flatMap(_.trim.toIntOption).getOrElse(19095)
        ).asJson
      )
  }
