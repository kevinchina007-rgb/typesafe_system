package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class GetAttractionDetailsPlannerRequest(attractionId: String, useDate: Option[String])
object GetAttractionDetailsPlannerRequest:
  given sourceEncoder: Encoder[GetAttractionDetailsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetAttractionDetailsPlannerRequest] = deriveDecoder
