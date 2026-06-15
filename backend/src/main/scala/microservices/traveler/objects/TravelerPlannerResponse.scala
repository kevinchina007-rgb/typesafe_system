// 本文件定义 traveler 模块的响应对象 `TravelerPlannerResponse`，仅负责数据结构与 JSON codec。
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerPlannerResponse(
    travelerId: String,
    ownerUserId: String,
    fullName: String,
    documentType: String,
    documentNumber: String,
    phone: String,
    birthDate: String,
    travelerType: String,
    status: String,
    isHidden: Boolean,
    isDefault: Boolean,
    basicInfo: TravelerBasicInfo,
    documentInfo: TravelerDocumentInfo,
    contactInfo: TravelerContactInfo,
    preferenceInfo: TravelerPreferenceInfo,
    specialRequirementInfo: TravelerSpecialRequirementInfo,
    serviceSummary: TravelerServiceSummary
)

object TravelerPlannerResponse:
  given sourceEncoder: Encoder[TravelerPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerPlannerResponse] = deriveDecoder
