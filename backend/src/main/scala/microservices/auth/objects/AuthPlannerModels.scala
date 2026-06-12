package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

// 注册用户接口的请求数据。
final case class SignupPlannerRequest(email: String, nickname: String, phone: String, password: String)
// 注册用户请求的 JSON codec。
object SignupPlannerRequest:
  given sourceEncoder: Encoder[SignupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SignupPlannerRequest] = deriveDecoder

// 用户登录接口的请求数据。
final case class LoginPlannerRequest(email: String, password: String)
// 用户登录请求的 JSON codec。
object LoginPlannerRequest:
  given sourceEncoder: Encoder[LoginPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LoginPlannerRequest] = deriveDecoder

// 只携带会话 ID 的请求数据。
final case class SessionPlannerRequest(sessionId: String)
// 会话请求的 JSON codec。
object SessionPlannerRequest:
  given sourceEncoder: Encoder[SessionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SessionPlannerRequest] = deriveDecoder

// 退出其他会话时使用的请求数据。
final case class LogoutOtherSessionsPlannerRequest(sessionId: String)
// 退出其他会话请求的 JSON codec。
object LogoutOtherSessionsPlannerRequest:
  given sourceEncoder: Encoder[LogoutOtherSessionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LogoutOtherSessionsPlannerRequest] = deriveDecoder

// 修改密码时使用的请求数据。
final case class ChangePasswordPlannerRequest(sessionId: String, currentPassword: String, newPassword: String)
// 修改密码请求的 JSON codec。
object ChangePasswordPlannerRequest:
  given sourceEncoder: Encoder[ChangePasswordPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ChangePasswordPlannerRequest] = deriveDecoder

// 当前用户登录态接口的响应数据。
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
// 当前用户响应的 JSON codec。
object CurrentUserPlannerResponse:
  given sourceEncoder: Encoder[CurrentUserPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[CurrentUserPlannerResponse] = deriveDecoder

// 用户会话列表中的单个会话数据。
final case class AuthSessionPlannerResponse(sessionId: String, createdAt: Instant, lastSeenAt: Instant, expiresAt: Instant, status: String)
// 单个会话响应的 JSON codec。
object AuthSessionPlannerResponse:
  given sourceEncoder: Encoder[AuthSessionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthSessionPlannerResponse] = deriveDecoder

// 用户会话列表接口的响应数据。
final case class AuthSessionListPlannerResponse(sessions: List[AuthSessionPlannerResponse])
// 会话列表响应的 JSON codec。
object AuthSessionListPlannerResponse:
  given sourceEncoder: Encoder[AuthSessionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthSessionListPlannerResponse] = deriveDecoder

// 会话状态接口的响应数据。
final case class AuthStatusPlannerResponse(status: String, revokedCount: Option[Int])
// 会话状态响应的 JSON codec。
object AuthStatusPlannerResponse:
  given sourceEncoder: Encoder[AuthStatusPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AuthStatusPlannerResponse] = deriveDecoder
