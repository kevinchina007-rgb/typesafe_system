package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionTicketSessionResponse(
    sessionId: String,
    sessionName: String,
    useDate: String,
    startsAt: String,
    endsAt: String,
    capacity: Int,
    availableQuantity: Int,
    status: String
)
object AttractionTicketSessionResponse:
  given sourceEncoder: Encoder[AttractionTicketSessionResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionTicketSessionResponse] = deriveDecoder
