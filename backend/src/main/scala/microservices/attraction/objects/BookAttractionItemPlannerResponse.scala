package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BookAttractionItemPlannerResponse(orderId: String, orderItemId: String)
object BookAttractionItemPlannerResponse:
  given sourceEncoder: Encoder[BookAttractionItemPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BookAttractionItemPlannerResponse] = deriveDecoder
