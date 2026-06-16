package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateAttractionTicketRulePlannerRequest(
    managerId: String,
    attractionId: String,
    ticketTypeId: String,
    ruleType: String,
    ageValue: Option[Int],
    minAge: Option[Int],
    maxAge: Option[Int],
    documentType: Option[String],
    documentNumberPrefix: Option[String]
)
object CreateAttractionTicketRulePlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionTicketRulePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionTicketRulePlannerRequest] = deriveDecoder

