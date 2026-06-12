// AttractionRuleJson 定义景点模块的规则 JSON 结构。

package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.parser.parse
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
          .orElse(decodeLegacyAgeLessThanRuleConfig(ruleConfigJson))
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.AgeBetween =>
        decode[TicketEligibilityRuleConfig.AgeBetween](ruleConfigJson)
          .orElse(decodeLegacyAgeBetweenRuleConfig(ruleConfigJson))
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.AgeAtLeast =>
        decode[TicketEligibilityRuleConfig.AgeAtLeast](ruleConfigJson)
          .orElse(decodeLegacyAgeAtLeastRuleConfig(ruleConfigJson))
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

  private def decodeLegacyAgeLessThanRuleConfig(ruleConfigJson: String): Either[Throwable, TicketEligibilityRuleConfig.AgeLessThan] =
    parse(ruleConfigJson).flatMap { json =>
      val cursor = json.hcursor
      cursor.downField("maxExclusive").as[Int].orElse(cursor.downField("ageValue").as[Int]).map(TicketEligibilityRuleConfig.AgeLessThan.apply)
    }

  private def decodeLegacyAgeBetweenRuleConfig(ruleConfigJson: String): Either[Throwable, TicketEligibilityRuleConfig.AgeBetween] =
    parse(ruleConfigJson).flatMap { json =>
      val cursor = json.hcursor
      for
        minInclusive <- cursor.downField("minInclusive").as[Int].orElse(cursor.downField("minAge").as[Int])
        maxInclusive <- cursor.downField("maxInclusive").as[Int].orElse(cursor.downField("maxAge").as[Int])
      yield TicketEligibilityRuleConfig.AgeBetween(minInclusive, maxInclusive)
    }

  private def decodeLegacyAgeAtLeastRuleConfig(ruleConfigJson: String): Either[Throwable, TicketEligibilityRuleConfig.AgeAtLeast] =
    parse(ruleConfigJson).flatMap { json =>
      val cursor = json.hcursor
      cursor.downField("minInclusive").as[Int].orElse(cursor.downField("ageValue").as[Int]).map(TicketEligibilityRuleConfig.AgeAtLeast.apply)
    }
