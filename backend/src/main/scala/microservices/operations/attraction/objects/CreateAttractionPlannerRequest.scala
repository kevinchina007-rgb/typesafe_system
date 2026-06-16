package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateAttractionPlannerRequest(managerId: String, attractionName: String, city: String, location: String, description: String, imageUrl: Option[String])
object CreateAttractionPlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionPlannerRequest] = deriveDecoder

