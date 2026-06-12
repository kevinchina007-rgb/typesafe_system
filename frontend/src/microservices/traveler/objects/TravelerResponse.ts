// 本文件定义 traveler 模块的 `TravelerResponse`，作为响应数据并提供 JSON 编解码。

import type {
  TravelerBasicInfo,
  TravelerContactInfo,
  TravelerDocumentInfo,
  TravelerPreferenceInfo,
  TravelerServiceSummary,
  TravelerSpecialRequirementInfo,
} from './TravelerProfileDetails'

export type TravelerResponse = {
  travelerId: string
  ownerUserId: string
  fullName: string
  documentType: string
  documentNumber: string
  phone: string
  birthDate: string
  travelerType: string
  status: string
  isHidden: boolean
  isDefault: boolean
  basicInfo: TravelerBasicInfo
  documentInfo: TravelerDocumentInfo
  contactInfo: TravelerContactInfo
  preferenceInfo: TravelerPreferenceInfo
  specialRequirementInfo: TravelerSpecialRequirementInfo
  serviceSummary: TravelerServiceSummary
}

export const travelerResponseFromJson = (json: string): TravelerResponse =>
  JSON.parse(json) as TravelerResponse

export const travelerResponseToJson = (value: TravelerResponse): string =>
  JSON.stringify(value)
