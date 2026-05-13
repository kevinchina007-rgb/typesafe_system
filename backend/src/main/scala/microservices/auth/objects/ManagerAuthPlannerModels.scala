package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class ManagerLoginPlannerRequest(managerType: String, email: String, password: String)
object ManagerLoginPlannerRequest:
  given Decoder[ManagerLoginPlannerRequest] = deriveDecoder

final case class ManagerSessionPlannerRequest(sessionId: String)
object ManagerSessionPlannerRequest:
  given Decoder[ManagerSessionPlannerRequest] = deriveDecoder

final case class ManagerChangePasswordPlannerRequest(sessionId: String, currentPassword: String, newPassword: String)
object ManagerChangePasswordPlannerRequest:
  given Decoder[ManagerChangePasswordPlannerRequest] = deriveDecoder

final case class CurrentManagerPlannerResponse(
    sessionId: String,
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    createdAt: Instant,
    expiresAt: Instant
)
object CurrentManagerPlannerResponse:
  given Encoder[CurrentManagerPlannerResponse] = deriveEncoder

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

final case class ManagerAuthSessionListPlannerResponse(sessions: List[ManagerAuthSessionPlannerResponse])
object ManagerAuthSessionListPlannerResponse:
  given Encoder[ManagerAuthSessionListPlannerResponse] = deriveEncoder

final case class ManagerAuthStatusPlannerResponse(status: String, revokedCount: Option[Int])
object ManagerAuthStatusPlannerResponse:
  given Encoder[ManagerAuthStatusPlannerResponse] = deriveEncoder
