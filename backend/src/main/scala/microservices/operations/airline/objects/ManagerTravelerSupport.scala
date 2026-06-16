// 本文件保留 airline 订单旅客明细所需的本地辅助对象，用于在 operations 域内部组装响应。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerTravelerBasicInfo(
    fullName: String,
    gender: String,
    birthDate: String,
    nationality: String
)
object ManagerTravelerBasicInfo:
  given sourceEncoder: Encoder[ManagerTravelerBasicInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerBasicInfo] = deriveDecoder

final case class ManagerTravelerDocumentInfo(
    documentType: String,
    documentNumber: String,
    documentExpiryDate: Option[String]
)
object ManagerTravelerDocumentInfo:
  given sourceEncoder: Encoder[ManagerTravelerDocumentInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerDocumentInfo] = deriveDecoder

final case class ManagerTravelerContactInfo(
    phone: String,
    email: Option[String]
)
object ManagerTravelerContactInfo:
  given sourceEncoder: Encoder[ManagerTravelerContactInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerContactInfo] = deriveDecoder

final case class ManagerTravelerPreferenceInfo(
    seatPreference: String,
    mealPreference: String,
    quietSeatPreferred: Boolean
)
object ManagerTravelerPreferenceInfo:
  given sourceEncoder: Encoder[ManagerTravelerPreferenceInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerPreferenceInfo] = deriveDecoder

final case class ManagerTravelerSpecialRequirementInfo(
    assistanceType: String,
    requirementNote: Option[String],
    hasLargeLuggage: Boolean,
    luggageNote: Option[String]
)
object ManagerTravelerSpecialRequirementInfo:
  given sourceEncoder: Encoder[ManagerTravelerSpecialRequirementInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerSpecialRequirementInfo] = deriveDecoder

final case class ManagerTravelerServiceSummary(
    age: Option[Int],
    documentLabel: String,
    contactLabel: String,
    preferenceLabel: String,
    requirementLabel: String,
    warningLevel: String
)
object ManagerTravelerServiceSummary:
  given sourceEncoder: Encoder[ManagerTravelerServiceSummary] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerServiceSummary] = deriveDecoder
