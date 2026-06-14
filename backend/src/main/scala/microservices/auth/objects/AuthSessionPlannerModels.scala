package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class SessionPlannerRequest(sessionId: String)
object SessionPlannerRequest:
  given sourceEncoder: Encoder[SessionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SessionPlannerRequest] = deriveDecoder

final case class UserSessionPlannerResponse(sessionId: String, createdAt: Instant, lastSeenAt: Instant, expiresAt: Instant, status: String)
object UserSessionPlannerResponse:
  given sourceEncoder: Encoder[UserSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UserSessionPlannerResponse] = deriveDecoder

final case class UserSessionListPlannerResponse(sessions: List[UserSessionPlannerResponse])
object UserSessionListPlannerResponse:
  given sourceEncoder: Encoder[UserSessionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UserSessionListPlannerResponse] = deriveDecoder


