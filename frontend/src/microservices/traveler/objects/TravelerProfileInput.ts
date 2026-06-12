// 本文件定义 traveler 模块的 `TravelerProfileInput`，作为输入模型并提供 JSON 编解码。

import type {
  TravelerBasicInfo,
  TravelerContactInfo,
  TravelerDocumentInfo,
  TravelerPreferenceInfo,
  TravelerSpecialRequirementInfo,
} from './TravelerProfileDetails'

export type TravelerProfileInput = {
  fullName: string
  documentType: string
  documentNumber: string
  phone: string
  birthDate: string
  seatPreference: string
  mealPreference: string
  accessibilityRequestNotes: string | null
  emergencyContactName: string | null
  emergencyContactPhoneNumber: string | null
  isDefaultTraveler: boolean
  basicInfo: TravelerBasicInfo | null
  documentInfo: TravelerDocumentInfo | null
  contactInfo: TravelerContactInfo | null
  preferenceInfo: TravelerPreferenceInfo | null
  specialRequirementInfo: TravelerSpecialRequirementInfo | null
}

export const travelerProfileInputFromJson = (json: string): TravelerProfileInput =>
  JSON.parse(json) as TravelerProfileInput

export const travelerProfileInputToJson = (value: TravelerProfileInput): string =>
  JSON.stringify(value)
