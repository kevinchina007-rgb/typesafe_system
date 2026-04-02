package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.{CurrentManagerSessionView, CurrentUserSessionView}

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
