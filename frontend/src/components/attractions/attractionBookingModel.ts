import type {
  AppLanguage,
  AttractionResponse,
  ResourceReviewSummaryResponse,
  ReviewResponse,
  TravelerResponse,
} from '../../lib/mvp-types'

export type AttractionQuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'holiday'
export type AttractionTypePreference = 'Nature' | 'Museum' | 'ThemePark' | 'Performance' | 'DayTour'
export type AttractionSortPreference = 'Popular' | 'Rating' | 'Price'

export type AttractionsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onSearchAttractions: (payload: {
    city?: string
    keyword?: string
    useDate?: string
    travelerCount?: number
    attractionType?: AttractionTypePreference
    sortPreference?: AttractionSortPreference
  }) => Promise<AttractionResponse[]>
  onBookAttraction: (payload: {
    attractionId: string
    ticketTypeId: string
    sessionId?: string | null
    travelerIds: string[]
    useDate: string
    orderCurrency: string
  }) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export const defaultAttractionSearchState = {
  city: '上海',
  keyword: '',
  useDate: '2026-04-10',
  travelerCount: 2,
  attractionType: 'ThemePark' as AttractionTypePreference,
  sortPreference: 'Popular' as AttractionSortPreference,
  selectedQuickDatePreset: null as AttractionQuickDatePreset | null,
}

export const attractionHotSpots = ['上海 迪士尼', '杭州 西湖', '东京 迪士尼海洋', '北京 故宫']
export const attractionRecentSearches = ['上海 迪士尼乐园', '北京 环球影城', '杭州 灵隐寺']
export const attractionTypeOptions: AttractionTypePreference[] = ['Nature', 'Museum', 'ThemePark', 'Performance', 'DayTour']
export const attractionSortOptions: AttractionSortPreference[] = ['Popular', 'Rating', 'Price']
export const attractionFilterOptions = ['priceRange', 'type', 'rating', 'distance', 'refundable', 'show', 'familyFriendly', 'tripLength'] as const

export function renderAttractionTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function formatAttractionRules(ruleSummaries: string[], translate: (translationKey: string) => string) {
  return ruleSummaries.length > 0 ? ruleSummaries.join(' · ') : translate('attractions.noRules')
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
