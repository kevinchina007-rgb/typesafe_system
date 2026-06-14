package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class ManagerSessionPlannerRequest(sessionId: String)
object ManagerSessionPlannerRequest:
  given Encoder[ManagerSessionPlannerRequest] = deriveEncoder
  given Decoder[ManagerSessionPlannerRequest] = deriveDecoder

final case class ManagerSessionPlannerResponse(
    sessionId: String,
    createdAt: Instant,
    lastSeenAt: Instant,
    expiresAt: Instant,
    status: String,
    isCurrent: Boolean
)
object ManagerSessionPlannerResponse:
  given Encoder[ManagerSessionPlannerResponse] = deriveEncoder
  given Decoder[ManagerSessionPlannerResponse] = deriveDecoder

final case class ManagerSessionListPlannerResponse(sessions: List[ManagerSessionPlannerResponse])
object ManagerSessionListPlannerResponse:
  given Encoder[ManagerSessionListPlannerResponse] = deriveEncoder
  given Decoder[ManagerSessionListPlannerResponse] = deriveDecoder


