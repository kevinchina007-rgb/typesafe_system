package com.typesafe.travel.api.dto

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
