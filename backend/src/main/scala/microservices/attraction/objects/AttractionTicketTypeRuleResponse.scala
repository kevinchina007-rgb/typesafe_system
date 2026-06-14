package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionTicketTypeRuleResponse(
    ruleId: String,
    ruleType: String,
    ageValue: Option[Int],
    minAge: Option[Int],
    maxAge: Option[Int],
    documentType: Option[String],
    documentNumberPrefix: Option[String],
    summary: String
)
object AttractionTicketTypeRuleResponse:
  given sourceEncoder: Encoder[AttractionTicketTypeRuleResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionTicketTypeRuleResponse] = deriveDecoder
