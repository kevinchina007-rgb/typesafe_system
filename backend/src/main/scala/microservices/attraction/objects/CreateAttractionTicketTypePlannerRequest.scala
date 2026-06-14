package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateAttractionTicketTypePlannerRequest(
    managerId: String,
    attractionId: String,
    ticketTypeName: String,
    description: String,
    unitPrice: String,
    currency: String,
    availableFromDate: String,
    availableToDate: String,
    totalQuantity: Int,
    validWeekdays: List[String]
)
object CreateAttractionTicketTypePlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionTicketTypePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionTicketTypePlannerRequest] = deriveDecoder
