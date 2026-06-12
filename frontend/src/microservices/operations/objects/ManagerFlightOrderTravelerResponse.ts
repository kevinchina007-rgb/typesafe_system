// 本文件定义 operations 模块的 `ManagerFlightOrderTravelerResponse`，作为响应数据并提供 JSON 编解码。

import type {
  TravelerBasicInfo,
  TravelerContactInfo,
  TravelerDocumentInfo,
  TravelerPreferenceInfo,
  TravelerServiceSummary,
  TravelerSpecialRequirementInfo,
} from '@/microservices/traveler/objects/TravelerProfileDetails'

export type ManagerFlightOrderTravelerResponse = {
  travelerId: string
  fullName: string
  documentNumber: string
  basicInfo: TravelerBasicInfo
  documentInfo: TravelerDocumentInfo
  contactInfo: TravelerContactInfo
  preferenceInfo: TravelerPreferenceInfo
  specialRequirementInfo: TravelerSpecialRequirementInfo
  serviceSummary: TravelerServiceSummary
}

export const managerFlightOrderTravelerResponseFromJson = (json: string): ManagerFlightOrderTravelerResponse =>
  JSON.parse(json) as ManagerFlightOrderTravelerResponse

export const managerFlightOrderTravelerResponseToJson = (value: ManagerFlightOrderTravelerResponse): string =>
  JSON.stringify(value)
