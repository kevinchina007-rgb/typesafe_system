package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionTicketTypeResponse(
    ticketTypeId: String,
    ticketTypeName: String,
    description: String,
    priceAmount: String,
    priceCurrency: String,
    status: String,
    availableFromDate: String,
    availableToDate: String,
    totalQuantity: Int,
    validWeekdays: List[String],
    availableQuantityForRequestedDate: Option[Int],
    isAvailableForRequestedDate: Boolean,
    rules: List[AttractionTicketTypeRuleResponse],
    sessions: List[AttractionTicketSessionResponse]
)
object AttractionTicketTypeResponse:
  given sourceEncoder: Encoder[AttractionTicketTypeResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionTicketTypeResponse] = deriveDecoder
