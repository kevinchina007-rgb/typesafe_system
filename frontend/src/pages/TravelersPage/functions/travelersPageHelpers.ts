import type { TravelerResponse } from '@/lib/mvp-types/index'
import type { TravelerFormDraft } from '../objects'
import type { TravelerProfileInput } from '@/microservices/traveler/objects/TravelerProfileInput'

export function normalizeDocumentType(value: string): string {
  const normalized = value.trim().toLowerCase()
  if (normalized.includes('identity')) return 'identity-card'
  if (normalized.includes('residence')) return 'residence-permit'
  if (normalized.includes('other')) return 'other'
  return 'passport'
}

export function normalizeSeatPreference(value: string): string {
  const normalized = value.trim().toLowerCase()
  if (normalized === 'window') return 'window'
  if (normalized === 'aisle') return 'aisle'
  if (normalized === 'middle') return 'middle'
  return 'none'
}

export function normalizeMealPreference(value: string): string {
  const normalized = value.trim().toLowerCase()
  if (normalized === 'vegetarian') return 'vegetarian'
  if (normalized === 'vegan') return 'vegan'
  if (normalized === 'halal') return 'halal'
  if (normalized === 'kosher') return 'kosher'
  if (normalized === 'childmeal' || normalized === 'child_meal') return 'childMeal'
  if (normalized === 'nopreference' || normalized === 'no_preference' || normalized === 'none') return 'none'
  return 'standard'
}

export function normalizeAssistanceType(value: string | null | undefined): string {
  const normalized = (value ?? '').trim()
  if (!normalized || normalized.toLowerCase() === 'none') return '无'
  return normalized
}

export function createTravelerFormDraft(traveler: TravelerResponse): TravelerFormDraft {
  return {
    travelerId: traveler.travelerId,
    fullName: traveler.basicInfo?.fullName ?? traveler.fullName,
    gender: traveler.basicInfo?.gender ?? '未填',
    nationality: traveler.basicInfo?.nationality ?? '中国',
    documentType: normalizeDocumentType(traveler.documentInfo?.documentType ?? traveler.documentType),
    documentNumber: traveler.documentInfo?.documentNumber ?? traveler.documentNumber,
    documentExpiryDate: traveler.documentInfo?.documentExpiryDate ?? '',
    phone: traveler.contactInfo?.phone ?? traveler.phone,
    email: traveler.contactInfo?.email ?? '',
    birthDate: traveler.basicInfo?.birthDate ?? traveler.birthDate,
    seatPreference: normalizeSeatPreference(traveler.preferenceInfo?.seatPreference ?? 'none'),
    mealPreference: normalizeMealPreference(traveler.preferenceInfo?.mealPreference ?? 'standard'),
    quietSeatPreferred: traveler.preferenceInfo?.quietSeatPreferred ?? false,
    assistanceType: normalizeAssistanceType(traveler.specialRequirementInfo?.assistanceType),
    requirementNote: traveler.specialRequirementInfo?.requirementNote ?? '',
    hasLargeLuggage: traveler.specialRequirementInfo?.hasLargeLuggage ?? false,
    luggageNote: traveler.specialRequirementInfo?.luggageNote ?? '',
    emergencyContactName: '',
    emergencyContactPhoneNumber: '',
    isDefaultTraveler: traveler.isDefault,
  }
}

export function buildTravelerPayload(draft: TravelerFormDraft): TravelerProfileInput {
  const requirementNote = draft.requirementNote.trim() || null
  const luggageNote = draft.luggageNote.trim() || null
  const email = draft.email.trim() || null
  return {
    fullName: draft.fullName.trim(),
    documentType: draft.documentType,
    documentNumber: draft.documentNumber.trim(),
    phone: draft.phone.trim(),
    birthDate: draft.birthDate,
    seatPreference: draft.seatPreference,
    mealPreference: draft.mealPreference,
    accessibilityRequestNotes: requirementNote,
    emergencyContactName: draft.emergencyContactName.trim() || null,
    emergencyContactPhoneNumber: draft.emergencyContactPhoneNumber.trim() || null,
    isDefaultTraveler: draft.isDefaultTraveler,
    basicInfo: {
      fullName: draft.fullName.trim(),
      gender: draft.gender,
      birthDate: draft.birthDate,
      nationality: draft.nationality.trim() || '中国',
    },
    documentInfo: {
      documentType: draft.documentType,
      documentNumber: draft.documentNumber.trim(),
      documentExpiryDate: draft.documentExpiryDate || null,
    },
    contactInfo: {
      phone: draft.phone.trim(),
      email,
    },
    preferenceInfo: {
      seatPreference: draft.seatPreference,
      mealPreference: draft.mealPreference,
      quietSeatPreferred: draft.quietSeatPreferred,
    },
    specialRequirementInfo: {
      assistanceType: draft.assistanceType,
      requirementNote,
      hasLargeLuggage: draft.hasLargeLuggage,
      luggageNote,
    },
  }
}

export function renderTravelerLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function labelForPreference(value: string): string {
  const labels: Record<string, string> = {
    Window: '靠窗',
    Aisle: '靠过道',
    Middle: '中间座',
    NoPreference: '无要求',
    window: '靠窗',
    aisle: '靠过道',
    middle: '中间座',
    none: '无要求',
  }
  return labels[value] ?? value
}

export function labelForMeal(value: string): string {
  const labels: Record<string, string> = {
    Standard: '标准餐',
    Vegetarian: '素食餐',
    Vegan: '纯素餐',
    Halal: '清真餐',
    Kosher: '犹太餐',
    ChildMeal: '儿童餐',
    NoPreference: '无要求',
    standard: '标准餐',
    vegetarian: '素食餐',
    vegan: '纯素餐',
    halal: '清真餐',
    kosher: '犹太餐',
    childMeal: '儿童餐',
    none: '无要求',
  }
  return labels[value] ?? value
}
