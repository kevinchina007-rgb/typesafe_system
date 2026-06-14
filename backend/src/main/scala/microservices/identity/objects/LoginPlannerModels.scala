package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class LoginPlannerRequest(email: String)
object LoginPlannerRequest:
  given sourceEncoder: Encoder[LoginPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LoginPlannerRequest] = deriveDecoder
