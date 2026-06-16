// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerFlightOrderTravelerResponse`锛屼綔涓哄搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

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

