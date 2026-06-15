// 本文件定义 traveler 模块的输入对象 `TravelerProfileInput`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerProfileInput(
    fullName: String,
    documentType: String,
    documentNumber: String,
    phone: String,
    birthDate: String,
    seatPreference: String,
    mealPreference: String,
    accessibilityRequestNotes: Option[String],
    emergencyContactName: Option[String],
    emergencyContactPhoneNumber: Option[String],
    isDefaultTraveler: Boolean,
    basicInfo: Option[TravelerBasicInfo] = None,
    documentInfo: Option[TravelerDocumentInfo] = None,
    contactInfo: Option[TravelerContactInfo] = None,
    preferenceInfo: Option[TravelerPreferenceInfo] = None,
    specialRequirementInfo: Option[TravelerSpecialRequirementInfo] = None
)

object TravelerProfileInput:
  given sourceEncoder: Encoder[TravelerProfileInput] = deriveEncoder
  given sourceDecoder: Decoder[TravelerProfileInput] = deriveDecoder
