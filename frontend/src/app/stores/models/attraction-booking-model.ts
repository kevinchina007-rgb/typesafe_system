import type { AttractionTicketTypeRuleResponse, AttractionResponse, TravelerResponse } from '@/lib/mvp-types/index'

export type AttractionQuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'holiday'
export type AttractionTypePreference = 'Nature' | 'Museum' | 'ThemePark' | 'Performance' | 'DayTour'
export type AttractionSortPreference = 'Popular' | 'Rating' | 'Price'

export const defaultAttractionSearchState = {
  city: '',
  keyword: '',
  useDate: '2026-04-10',
  travelerCount: 2,
  attractionType: 'ThemePark' as AttractionTypePreference,
  sortPreference: 'Popular' as AttractionSortPreference,
  selectedQuickDatePreset: null as AttractionQuickDatePreset | null,
}

export const attractionHotSpots = ['\u7ebd\u7ea6 \u53cc\u5b50\u5854', '上海 迪士尼', '杭州 西湖', '东京 迪士尼海洋', '北京 故宫']
export const attractionRecentSearches = ['\u7ebd\u7ea6 \u53cc\u5b50\u5854', '上海 迪士尼乐园', '北京 环球影城', '杭州 灵隐寺']
export const attractionTypeOptions: AttractionTypePreference[] = ['Nature', 'Museum', 'ThemePark', 'Performance', 'DayTour']
export const attractionSortOptions: AttractionSortPreference[] = ['Popular', 'Rating', 'Price']
export const attractionFilterOptions = ['priceRange', 'type', 'rating', 'distance', 'refundable', 'show', 'familyFriendly', 'tripLength'] as const

export type AttractionTravelerEligibility = {
  travelerId: string
  eligible: boolean
  failureReasons: string[]
  ageOnUseDate: number | null
}

export function renderAttractionTravelerOptionLabel(traveler: TravelerResponse, useDate?: string): string {
  const baseLabel = `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
  if (!useDate) {
    return baseLabel
  }
  const ageLabel = getTravelerAgeOnDate(traveler.birthDate, useDate)
  return ageLabel === null ? baseLabel : `${baseLabel} · ${ageLabel}岁`
}

export function formatAttractionRules(ruleSummaries: string[], translate: (translationKey: string) => string) {
  return ruleSummaries.length > 0 ? ruleSummaries.join(' / ') : translate('attractions.noRules')
}

export function filterAttractionSessionsForUseDate<T extends { useDate: string }>(sessions: T[], useDate: string) {
  return sessions.filter(session => session.useDate === useDate)
}

export function applyAttractionQuickDatePreset(preset: AttractionQuickDatePreset, today = new Date()) {
  const baseDate = new Date(today)
  if (preset === 'tomorrow') {
    baseDate.setDate(baseDate.getDate() + 1)
  }
  if (preset === 'weekend') {
    const day = baseDate.getDay()
    const daysUntilSaturday = (6 - day + 7) % 7
    baseDate.setDate(baseDate.getDate() + daysUntilSaturday)
  }
  if (preset === 'holiday') {
    baseDate.setDate(baseDate.getDate() + 14)
  }
  return baseDate.toISOString().slice(0, 10)
}

export function formatAttractionInsight(attractions: AttractionResponse[], translate: (translationKey: string) => string) {
  if (attractions.length === 0) {
    return translate('attractions.insightFallback')
  }
  return translate('attractions.insightValue').replace('{name}', attractions[0].attractionName)
}

export function getTravelerAgeOnDate(birthDate: string, useDate: string): number | null {
  if (!birthDate || !useDate) {
    return null
  }
  const parsedBirthDate = new Date(birthDate)
  const parsedUseDate = new Date(useDate)
  if (Number.isNaN(parsedBirthDate.getTime()) || Number.isNaN(parsedUseDate.getTime())) {
    return null
  }

  let age = parsedUseDate.getFullYear() - parsedBirthDate.getFullYear()
  const hasBirthdayPassed =
    parsedUseDate.getMonth() > parsedBirthDate.getMonth() ||
    (parsedUseDate.getMonth() === parsedBirthDate.getMonth() && parsedUseDate.getDate() >= parsedBirthDate.getDate())
  if (!hasBirthdayPassed) {
    age -= 1
  }
  return age >= 0 ? age : null
}

export function evaluateAttractionTravelerEligibility(
  traveler: TravelerResponse,
  rules: AttractionTicketTypeRuleResponse[],
  useDate: string,
): AttractionTravelerEligibility {
  const failureReasons: string[] = []
  const ageOnUseDate = getTravelerAgeOnDate(traveler.birthDate, useDate)
  const normalizedDocumentType = traveler.documentType.trim().toLowerCase()
  const normalizedDocumentNumber = traveler.documentNumber.trim().toLowerCase()

  for (const rule of rules) {
    const normalizedRuleType = rule.ruleType.trim()
    if (normalizedRuleType === 'AgeLessThan') {
      const maxExclusive = rule.ageValue ?? 0
      if (ageOnUseDate === null || ageOnUseDate >= maxExclusive) {
        failureReasons.push(`年龄必须小于 ${maxExclusive} 岁`)
      }
      continue
    }
    if (normalizedRuleType === 'AgeBetween') {
      const minAge = rule.minAge ?? 0
      const maxAge = rule.maxAge ?? 0
      if (ageOnUseDate === null || ageOnUseDate < minAge || ageOnUseDate > maxAge) {
        failureReasons.push(`年龄必须在 ${minAge}-${maxAge} 岁之间`)
      }
      continue
    }
    if (normalizedRuleType === 'AgeAtLeast') {
      const minAge = rule.minAge ?? rule.ageValue ?? 0
      if (ageOnUseDate === null || ageOnUseDate < minAge) {
        failureReasons.push(`年龄必须至少 ${minAge} 岁`)
      }
      continue
    }
    if (normalizedRuleType === 'DocumentTypeEquals') {
      const expectedDocumentType = (rule.documentType ?? '').trim().toLowerCase()
      if (expectedDocumentType && normalizedDocumentType !== expectedDocumentType) {
        failureReasons.push(`证件类型必须是 ${rule.documentType}`)
      }
      continue
    }
    if (normalizedRuleType === 'DocumentNumberPrefix') {
      const prefix = (rule.documentNumberPrefix ?? '').trim().toLowerCase()
      if (prefix && !normalizedDocumentNumber.startsWith(prefix)) {
        failureReasons.push(`证件号必须以 ${rule.documentNumberPrefix} 开头`)
      }
    }
  }

  return {
    travelerId: traveler.travelerId,
    eligible: failureReasons.length === 0,
    failureReasons,
    ageOnUseDate,
  }
}

export function evaluateAttractionTravelersEligibility(
  travelers: TravelerResponse[],
  rules: AttractionTicketTypeRuleResponse[],
  useDate: string,
) {
  return travelers.map(traveler => ({
    traveler,
    eligibility: evaluateAttractionTravelerEligibility(traveler, rules, useDate),
  }))
}

export function summarizeAttractionEligibilityFailure(
  travelers: TravelerResponse[],
  selectedTravelerIds: string[],
  rules: AttractionTicketTypeRuleResponse[],
  useDate: string,
) {
  const selectedTravelers = travelers.filter(traveler => selectedTravelerIds.includes(traveler.travelerId))
  if (selectedTravelers.length === 0) {
    return 'invalid_traveler_selection: Please choose at least one traveler.'
  }

  const invalidDetails = evaluateAttractionTravelersEligibility(selectedTravelers, rules, useDate)
    .filter(item => !item.eligibility.eligible)
    .map(item => `${item.traveler.fullName}: ${item.eligibility.failureReasons.join('；')}`)

  if (invalidDetails.length > 0) {
    return `traveler_not_eligible: ${invalidDetails.join(' / ')}`
  }

  return null
}
