package com.typesafe.travel.api.dto

import com.typesafe.travel.identity.domain.User

final case class CreateUserRequestDto(
    email: String,
    nickname: String,
    phone: String
)

final case class LoginUserRequestDto(
    email: String
)

final case class UserResponseDto(
    userId: String,
    email: String,
    nickname: String,
    phone: String,
    avatarUrl: Option[String],
    status: String,
    membershipLevel: String,
    points: Long,
    defaultTravelerProfileId: Option[String],
    createdAt: String
)

object UserResponseDto:
  def fromDomain(user: User): UserResponseDto =
    UserResponseDto(
      userId = user.userId.value,
      email = user.primaryEmailAddress.value,
      nickname = user.userDisplayName.value,
      phone = user.userPhoneNumber.value,
      avatarUrl = user.avatarUrl.map(_.value),
      status = user.userAccountStatus.toString,
      membershipLevel = user.membershipLevel.toString,
      points = user.loyaltyPoints.value,
      defaultTravelerProfileId = user.defaultTravelerProfileId.map(_.value),
      createdAt = user.registeredAt.toString
    )
