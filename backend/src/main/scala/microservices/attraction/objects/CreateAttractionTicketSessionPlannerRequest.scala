package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateAttractionTicketSessionPlannerRequest(
    managerId: String,
    attractionId: String,
    ticketTypeId: String,
    sessionName: String,
    useDate: String,
    startsAt: String,
    endsAt: String,
    capacity: Int
)
object CreateAttractionTicketSessionPlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionTicketSessionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionTicketSessionPlannerRequest] = deriveDecoder
