package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UpdateUserProfilePlannerRequest(userId: String, nickname: String, phone: String)
object UpdateUserProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateUserProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateUserProfilePlannerRequest] = deriveDecoder
