package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetUserPlannerRequest(userId: String)
object GetUserPlannerRequest:
  given sourceEncoder: Encoder[GetUserPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetUserPlannerRequest] = deriveDecoder
