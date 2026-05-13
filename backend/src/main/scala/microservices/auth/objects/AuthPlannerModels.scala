package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class SignupPlannerRequest(email: String, nickname: String, phone: String, password: String)
object SignupPlannerRequest:
  given sourceEncoder: Encoder[SignupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SignupPlannerRequest] = deriveDecoder

final case class LoginPlannerRequest(email: String, password: String)
object LoginPlannerRequest:
  given sourceEncoder: Encoder[LoginPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LoginPlannerRequest] = deriveDecoder

final case class SessionPlannerRequest(sessionId: String)
object SessionPlannerRequest:
  given sourceEncoder: Encoder[SessionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SessionPlannerRequest] = deriveDecoder

final case class LogoutOtherSessionsPlannerRequest(sessionId: String)
object LogoutOtherSessionsPlannerRequest:
  given sourceEncoder: Encoder[LogoutOtherSessionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LogoutOtherSessionsPlannerRequest] = deriveDecoder

final case class ChangePasswordPlannerRequest(sessionId: String, currentPassword: String, newPassword: String)
object ChangePasswordPlannerRequest:
  given sourceEncoder: Encoder[ChangePasswordPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ChangePasswordPlannerRequest] = deriveDecoder

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

final case class AuthSessionPlannerResponse(sessionId: String, createdAt: Instant, lastSeenAt: Instant, expiresAt: Instant, status: String)
object AuthSessionPlannerResponse:
  given sourceEncoder: Encoder[AuthSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthSessionPlannerResponse] = deriveDecoder

final case class AuthSessionListPlannerResponse(sessions: List[AuthSessionPlannerResponse])
object AuthSessionListPlannerResponse:
  given sourceEncoder: Encoder[AuthSessionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthSessionListPlannerResponse] = deriveDecoder

final case class AuthStatusPlannerResponse(status: String, revokedCount: Option[Int])
object AuthStatusPlannerResponse:
  given sourceEncoder: Encoder[AuthStatusPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthStatusPlannerResponse] = deriveDecoder
