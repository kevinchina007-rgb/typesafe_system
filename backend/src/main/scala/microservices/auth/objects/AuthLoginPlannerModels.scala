package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class LoginPlannerRequest(email: String, password: String)
object LoginPlannerRequest:
  given sourceEncoder: Encoder[LoginPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LoginPlannerRequest] = deriveDecoder

final case class CurrentUserPlannerResponse(
    sessionId: String,
    userId: String,
    email: String,
    nickname: String,
    phone: String,
    avatarUrl: Option[String],
    membershipLevel: String,
    points: Long,
    expiresAt: Instant
)
object CurrentUserPlannerResponse:
  given sourceEncoder: Encoder[CurrentUserPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[CurrentUserPlannerResponse] = deriveDecoder


