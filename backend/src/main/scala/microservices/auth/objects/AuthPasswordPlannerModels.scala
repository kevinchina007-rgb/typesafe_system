package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class LogoutOtherSessionsPlannerRequest(sessionId: String)
object LogoutOtherSessionsPlannerRequest:
  given sourceEncoder: Encoder[LogoutOtherSessionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LogoutOtherSessionsPlannerRequest] = deriveDecoder

final case class ChangePasswordPlannerRequest(sessionId: String, currentPassword: String, newPassword: String)
object ChangePasswordPlannerRequest:
  given sourceEncoder: Encoder[ChangePasswordPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ChangePasswordPlannerRequest] = deriveDecoder

final case class AuthStatusPlannerResponse(status: String, revokedCount: Option[Int])
object AuthStatusPlannerResponse:
  given sourceEncoder: Encoder[AuthStatusPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthStatusPlannerResponse] = deriveDecoder


