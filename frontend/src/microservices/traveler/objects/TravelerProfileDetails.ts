// 本文件集中定义 traveler 旅客档案的公共细分信息，供前端旅客对象与页面复用。
export type TravelerBasicInfo = {
  fullName: string
  gender: string
  birthDate: string
  nationality: string
}

export const travelerBasicInfoFromJson = (json: string): TravelerBasicInfo =>
  JSON.parse(json) as TravelerBasicInfo

export const travelerBasicInfoToJson = (value: TravelerBasicInfo): string =>
  JSON.stringify(value)

export type TravelerContactInfo = {
  phone: string
  email: string | null
}

export const travelerContactInfoFromJson = (json: string): TravelerContactInfo =>
  JSON.parse(json) as TravelerContactInfo

export const travelerContactInfoToJson = (value: TravelerContactInfo): string =>
  JSON.stringify(value)

export type TravelerDocumentInfo = {
  documentType: string
  documentNumber: string
  documentExpiryDate: string | null
}

export const travelerDocumentInfoFromJson = (json: string): TravelerDocumentInfo =>
  JSON.parse(json) as TravelerDocumentInfo

export const travelerDocumentInfoToJson = (value: TravelerDocumentInfo): string =>
  JSON.stringify(value)

export type TravelerPreferenceInfo = {
  seatPreference: string
  mealPreference: string
  quietSeatPreferred: boolean
}

export const travelerPreferenceInfoFromJson = (json: string): TravelerPreferenceInfo =>
  JSON.parse(json) as TravelerPreferenceInfo

export const travelerPreferenceInfoToJson = (value: TravelerPreferenceInfo): string =>
  JSON.stringify(value)

export type TravelerServiceSummary = {
  age: number | null
  documentLabel: string
  contactLabel: string
  preferenceLabel: string
  requirementLabel: string
  warningLevel: string
}

export const travelerServiceSummaryFromJson = (json: string): TravelerServiceSummary =>
  JSON.parse(json) as TravelerServiceSummary

export const travelerServiceSummaryToJson = (value: TravelerServiceSummary): string =>
  JSON.stringify(value)

export type TravelerSpecialRequirementInfo = {
  assistanceType: string
  requirementNote: string | null
  hasLargeLuggage: boolean
  luggageNote: string | null
}

export const travelerSpecialRequirementInfoFromJson = (json: string): TravelerSpecialRequirementInfo =>
  JSON.parse(json) as TravelerSpecialRequirementInfo

export const travelerSpecialRequirementInfoToJson = (value: TravelerSpecialRequirementInfo): string =>
  JSON.stringify(value)
