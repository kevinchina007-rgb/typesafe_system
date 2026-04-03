package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.{AuthSessionView, CurrentManagerSessionView, CurrentUserSessionView}

final case class SignupRequestDto(
    email: String,
    nickname: String,
    phone: String,
    password: String
)

final case class PasswordLoginRequestDto(
    email: String,
    password: String
)

final case class ManagerPasswordLoginRequestDto(
    managerType: String,
    email: String,
    password: String
)

final case class ChangePasswordRequestDto(
    currentPassword: String,
    newPassword: String
)

final case class CurrentUserSessionResponseDto(
    user: UserResponseDto,
    expiresAt: String
)

final case class CurrentManagerSessionResponseDto(
    managerId: String,
    managerType: String,
    email: String,
    displayName: String,
    status: String,
    scopeId: String,
    createdAt: String,
    expiresAt: String
)

final case class AuthSessionResponseDto(
    sessionId: String,
    createdAt: String,
    lastSeenAt: String,
    expiresAt: String,
    status: String,
    isCurrent: Boolean
)

final case class AuthSessionListResponseDto(
    sessions: List[AuthSessionResponseDto]
)

final case class LogoutOtherSessionsResponseDto(
    revokedCount: Int
)

object CurrentUserSessionResponseDto:
  def fromView(view: CurrentUserSessionView): CurrentUserSessionResponseDto =
    CurrentUserSessionResponseDto(
      user = UserResponseDto.fromDomain(view.user),
      expiresAt = view.expiresAt.toString
    )

object CurrentManagerSessionResponseDto:
  def fromView(view: CurrentManagerSessionView): CurrentManagerSessionResponseDto =
    CurrentManagerSessionResponseDto(
      managerId = view.managerId.value,
      managerType = view.managerType.toString,
      email = view.emailAddress.value,
      displayName = view.displayName.value,
      status = view.statusLabel,
      scopeId = view.scopeId,
      createdAt = view.createdAt.toString,
      expiresAt = view.expiresAt.toString
    )

object AuthSessionResponseDto:
  def fromView(view: AuthSessionView): AuthSessionResponseDto =
    AuthSessionResponseDto(
      sessionId = view.sessionId.value,
      createdAt = view.createdAt.toString,
      lastSeenAt = view.lastSeenAt.toString,
      expiresAt = view.expiresAt.toString,
      status = view.status.toString,
      isCurrent = view.isCurrent
    )
