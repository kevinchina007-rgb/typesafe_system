package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionResponse(
    attractionId: String,
    attractionName: String,
    city: String,
    location: String,
    description: String,
    imageUrl: Option[String],
    status: String,
    ticketTypes: List[AttractionTicketTypeResponse]
)
object AttractionResponse:
  given sourceEncoder: Encoder[AttractionResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionResponse] = deriveDecoder
