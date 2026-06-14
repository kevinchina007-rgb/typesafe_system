package com.typesafe.travel.auth.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SignupPlannerRequest(email: String, nickname: String, phone: String, password: String)
object SignupPlannerRequest:
  given sourceEncoder: Encoder[SignupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SignupPlannerRequest] = deriveDecoder


