package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateUserPlannerRequest(email: String, nickname: String, phone: String)
object CreateUserPlannerRequest:
  given sourceEncoder: Encoder[CreateUserPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateUserPlannerRequest] = deriveDecoder
