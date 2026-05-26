export type TravelerBasicInfo = {
  fullName: string
  gender: string
  birthDate: string
  nationality: string
}

export type TravelerDocumentInfo = {
  documentType: string
  documentNumber: string
  documentExpiryDate: string | null
}

export type TravelerContactInfo = {
  phone: string
  email: string | null
}

export type TravelerPreferenceInfo = {
  seatPreference: string
  mealPreference: string
  quietSeatPreferred: boolean
}

export type TravelerSpecialRequirementInfo = {
  assistanceType: string
  requirementNote: string | null
  hasLargeLuggage: boolean
  luggageNote: string | null
}

export type TravelerServiceSummary = {
  age: number | null
  documentLabel: string
  contactLabel: string
  preferenceLabel: string
  requirementLabel: string
  warningLevel: string
}
