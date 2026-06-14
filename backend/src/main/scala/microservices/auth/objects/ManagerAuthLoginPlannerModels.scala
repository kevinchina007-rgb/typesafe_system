package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class ManagerLoginPlannerRequest(managerType: String, email: String, password: String)
object ManagerLoginPlannerRequest:
  given Encoder[ManagerLoginPlannerRequest] = deriveEncoder
  given Decoder[ManagerLoginPlannerRequest] = deriveDecoder

final case class CurrentManagerPlannerResponse(
    sessionId: String,
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    logoAssetPath: Option[String],
    createdAt: Instant,
    expiresAt: Instant
)
object CurrentManagerPlannerResponse:
  given Encoder[CurrentManagerPlannerResponse] = deriveEncoder
  given Decoder[CurrentManagerPlannerResponse] = deriveDecoder


