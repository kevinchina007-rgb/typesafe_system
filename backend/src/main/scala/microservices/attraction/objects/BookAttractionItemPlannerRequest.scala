package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BookAttractionItemPlannerRequest(
    userId: String,
    orderId: String,
    attractionId: String,
    ticketTypeId: String,
    sessionId: Option[String],
    travelerIds: List[String],
    useDate: String
)
object BookAttractionItemPlannerRequest:
  given sourceEncoder: Encoder[BookAttractionItemPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookAttractionItemPlannerRequest] = deriveDecoder
