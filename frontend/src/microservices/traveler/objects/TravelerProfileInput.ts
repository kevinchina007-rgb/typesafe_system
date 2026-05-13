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
}
