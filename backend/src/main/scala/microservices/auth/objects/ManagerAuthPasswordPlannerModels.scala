package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerChangePasswordPlannerRequest(sessionId: String, currentPassword: String, newPassword: String)
object ManagerChangePasswordPlannerRequest:
  given Encoder[ManagerChangePasswordPlannerRequest] = deriveEncoder
  given Decoder[ManagerChangePasswordPlannerRequest] = deriveDecoder

final case class ManagerAuthStatusPlannerResponse(status: String, revokedCount: Option[Int])
object ManagerAuthStatusPlannerResponse:
  given Encoder[ManagerAuthStatusPlannerResponse] = deriveEncoder
  given Decoder[ManagerAuthStatusPlannerResponse] = deriveDecoder


