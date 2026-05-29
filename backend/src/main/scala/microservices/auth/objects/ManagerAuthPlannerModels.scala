package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class ManagerLoginPlannerRequest(managerType: String, email: String, password: String)
object ManagerLoginPlannerRequest:
  given Encoder[ManagerLoginPlannerRequest] = deriveEncoder
  given Decoder[ManagerLoginPlannerRequest] = deriveDecoder

final case class ManagerSessionPlannerRequest(sessionId: String)
object ManagerSessionPlannerRequest:
  given Encoder[ManagerSessionPlannerRequest] = deriveEncoder
  given Decoder[ManagerSessionPlannerRequest] = deriveDecoder

final case class ManagerChangePasswordPlannerRequest(sessionId: String, currentPassword: String, newPassword: String)
object ManagerChangePasswordPlannerRequest:
  given Encoder[ManagerChangePasswordPlannerRequest] = deriveEncoder
  given Decoder[ManagerChangePasswordPlannerRequest] = deriveDecoder

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

final case class ManagerAuthSessionPlannerResponse(
    sessionId: String,
    createdAt: Instant,
    lastSeenAt: Instant,
    expiresAt: Instant,
    status: String,
    isCurrent: Boolean
)
object ManagerAuthSessionPlannerResponse:
  given Encoder[ManagerAuthSessionPlannerResponse] = deriveEncoder
  given Decoder[ManagerAuthSessionPlannerResponse] = deriveDecoder

final case class ManagerAuthSessionListPlannerResponse(sessions: List[ManagerAuthSessionPlannerResponse])
object ManagerAuthSessionListPlannerResponse:
  given Encoder[ManagerAuthSessionListPlannerResponse] = deriveEncoder
  given Decoder[ManagerAuthSessionListPlannerResponse] = deriveDecoder

final case class ManagerAuthStatusPlannerResponse(status: String, revokedCount: Option[Int])
object ManagerAuthStatusPlannerResponse:
  given Encoder[ManagerAuthStatusPlannerResponse] = deriveEncoder
  given Decoder[ManagerAuthStatusPlannerResponse] = deriveDecoder
