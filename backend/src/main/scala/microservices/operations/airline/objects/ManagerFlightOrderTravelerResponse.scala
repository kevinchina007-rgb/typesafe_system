// 本文件定义 airline 订单中旅客明细的响应结构，用于航司管理端查看订单。
package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ManagerFlightOrderTravelerResponse(
    travelerId: String,
    fullName: String,
    documentNumber: String,
    basicInfo: ManagerTravelerBasicInfo,
    documentInfo: ManagerTravelerDocumentInfo,
    contactInfo: ManagerTravelerContactInfo,
    preferenceInfo: ManagerTravelerPreferenceInfo,
    specialRequirementInfo: ManagerTravelerSpecialRequirementInfo,
    serviceSummary: ManagerTravelerServiceSummary
)
object ManagerFlightOrderTravelerResponse:
  given sourceEncoder: Encoder[ManagerFlightOrderTravelerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrderTravelerResponse] = deriveDecoder
