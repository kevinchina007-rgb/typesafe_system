// 本文件定义 traveler 模块的旅客响应对象，供前端直接使用。
import type {
  TravelerBasicInfo,
  TravelerContactInfo,
  TravelerDocumentInfo,
  TravelerPreferenceInfo,
  TravelerServiceSummary,
  TravelerSpecialRequirementInfo,
} from './TravelerProfileDetails'

export type TravelerPlannerResponse = {
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

export type TravelerResponse = TravelerPlannerResponse

export const travelerPlannerResponseFromJson = (json: string): TravelerPlannerResponse =>
  JSON.parse(json) as TravelerPlannerResponse

export const travelerPlannerResponseToJson = (value: TravelerPlannerResponse): string =>
  JSON.stringify(value)

export const travelerResponseFromJson = travelerPlannerResponseFromJson
export const travelerResponseToJson = travelerPlannerResponseToJson
