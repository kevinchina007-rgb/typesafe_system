package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UserPlannerResponse(
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
object UserPlannerResponse:
  given sourceEncoder: Encoder[UserPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UserPlannerResponse] = deriveDecoder
