package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.parser.decode
import io.circe.syntax.*

object AttractionRuleJson:
  given Encoder[TravelerDocumentType] = Encoder.encodeString.contramap(_.toString)
  given Decoder[TravelerDocumentType] = Decoder.decodeString.emap { rawValue =>
    TravelerDocumentType.all
      .find(_.toString == rawValue)
      .toRight(s"Unknown traveler document type: $rawValue")
  }

  given Encoder[TicketEligibilityRuleConfig.AgeLessThan] = deriveEncoder
  given Decoder[TicketEligibilityRuleConfig.AgeLessThan] = deriveDecoder
  given Encoder[TicketEligibilityRuleConfig.AgeBetween] = deriveEncoder
  given Decoder[TicketEligibilityRuleConfig.AgeBetween] = deriveDecoder
  given Encoder[TicketEligibilityRuleConfig.AgeAtLeast] = deriveEncoder
  given Decoder[TicketEligibilityRuleConfig.AgeAtLeast] = deriveDecoder
  given Encoder[TicketEligibilityRuleConfig.DocumentTypeEquals] = deriveEncoder
  given Decoder[TicketEligibilityRuleConfig.DocumentTypeEquals] = deriveDecoder
  given Encoder[TicketEligibilityRuleConfig.DocumentNumberPrefix] = deriveEncoder
  given Decoder[TicketEligibilityRuleConfig.DocumentNumberPrefix] = deriveDecoder

  def encodeRuleConfig(ruleConfig: TicketEligibilityRuleConfig): String =
    ruleConfig match
      case value: TicketEligibilityRuleConfig.AgeLessThan          => value.asJson.noSpaces
      case value: TicketEligibilityRuleConfig.AgeBetween           => value.asJson.noSpaces
      case value: TicketEligibilityRuleConfig.AgeAtLeast           => value.asJson.noSpaces
      case value: TicketEligibilityRuleConfig.DocumentTypeEquals   => value.asJson.noSpaces
      case value: TicketEligibilityRuleConfig.DocumentNumberPrefix => value.asJson.noSpaces

  def decodeRuleConfig(ticketTypeId: TicketTypeId, ruleType: TicketEligibilityRuleType, ruleConfigJson: String): Either[AttractionError, TicketEligibilityRuleConfig] =
    ruleType match
      case TicketEligibilityRuleType.AgeLessThan =>
        decode[TicketEligibilityRuleConfig.AgeLessThan](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.AgeBetween =>
        decode[TicketEligibilityRuleConfig.AgeBetween](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.AgeAtLeast =>
        decode[TicketEligibilityRuleConfig.AgeAtLeast](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.DocumentTypeEquals =>
        decode[TicketEligibilityRuleConfig.DocumentTypeEquals](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.DocumentNumberPrefix =>
        decode[TicketEligibilityRuleConfig.DocumentNumberPrefix](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
