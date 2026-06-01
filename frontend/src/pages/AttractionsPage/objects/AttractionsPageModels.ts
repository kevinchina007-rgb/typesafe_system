import type { AppLanguage, AttractionResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AppViewKey } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { AttractionQuickDatePreset, AttractionSortPreference, AttractionTypePreference } from '@/app/stores/models/attraction-booking-model'
import type { AttractionTicketTypeRuleResponse } from '@/lib/mvp-types/resources'

export type AttractionsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export type AttractionsSearchRequest = {
  city?: string
  keyword?: string
  useDate?: string
  sortPreference?: AttractionSortPreference
}

export type AttractionBookingPayload = {
  attractionId: string
  attractionName: string
  ticketTypeId: string
  ticketTypeName: string
  sessionId?: string | null
  travelerIds: string[]
  useDate: string
  orderCurrency: string
  rules: AttractionTicketTypeRuleResponse[]
}

export type AttractionsPageController = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  deliveryAdvertisements: AdvertisementResponse[]
  attractionResponses: AttractionResponse[]
  hasSearchedAttractions: boolean
  searchCity: string
  keyword: string
  useDateDraft: string
  travelerCount: number
  attractionType: AttractionTypePreference
  sortPreference: AttractionSortPreference
  selectedQuickDatePreset: AttractionQuickDatePreset | null
  dateWindowStart: string
  isAuthDialogOpen: boolean
  setSearchCity: (value: string) => void
  setKeyword: (value: string) => void
  setUseDateDraft: (value: string) => void
  setTravelerCount: (value: number) => void
  setAttractionType: (value: AttractionTypePreference) => void
  setSortPreference: (value: AttractionSortPreference) => void
  setSelectedQuickDatePreset: (value: AttractionQuickDatePreset | null) => void
  toggleTravelerSelection: (travelerId: string) => void
  setAttractionResponses: (value: AttractionResponse[]) => void
  setHasSearchedAttractions: (value: boolean) => void
  handleSearchAttractions: () => Promise<void>
  onPreviousDateWindow: () => void
  onNextDateWindow: () => void
  handleDateSelect: (date: string) => Promise<void>
  handleSelectHotAttraction: (value: string) => void
  handleOpenAdvertisement: (advertisement: AdvertisementResponse) => Promise<void>
  handleBookAttraction: (payload: AttractionBookingPayload) => Promise<void>
  handleLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  handleLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
  handleSelectQuickDatePreset: (preset: AttractionQuickDatePreset, nextDate: string) => void
  handleOpenAuthDialog: () => void
  handleCloseAuthDialog: () => void
  handleConfirmAuthDialog: () => void
}

export type AttractionPageHeroProps = {
  title: string
  description: string
}

export type AttractionSearchCardProps = {
  isBusy: boolean
  searchCity: string
  keyword: string
  useDateDraft: string
  selectedQuickDatePreset: AttractionQuickDatePreset | null
  hotAttractions: string[]
  recentSearches: string[]
  insight: string
  translate: (translationKey: string) => string
  onSearchCityChange: (value: string) => void
  onKeywordChange: (value: string) => void
  onUseDateChange: (value: string) => void
  onSelectQuickDatePreset: (preset: AttractionQuickDatePreset, nextDate: string) => void
  onSelectHotAttraction: (value: string) => void
  onSearch: () => void
}

export type AttractionFilterBarProps = {
  hasSearchedAttractions: boolean
  sortPreference: AttractionSortPreference
  translate: (translationKey: string) => string
  onSortPreferenceChange: (value: AttractionSortPreference) => void
}

export type AttractionResultsSectionProps = {
  attractionResponses: AttractionResponse[]
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  translate: (translationKey: string) => string
  useDateDraft: string
  onToggleTravelerSelection: (travelerId: string) => void
  onRequireLogin: () => void
  onBookAttraction: (payload: AttractionBookingPayload) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export type AttractionResultCardProps = {
  attractionResponse: AttractionResponse
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  translate: (translationKey: string) => string
  useDateDraft: string
  onRequireLogin: () => void
  onBookAttraction: (payload: AttractionBookingPayload) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export type AttractionTypeSelectorProps = {
  value: AttractionTypePreference
  translate: (translationKey: string) => string
  onChange: (value: AttractionTypePreference) => void
}

export type AttractionKeywordInputProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

export type CitySelectorProps = {
  value: string
  translate: (translationKey: string) => string
  suggestions: string[]
  onChange: (value: string) => void
}

export type DateSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

export type HotAttractionsProps = {
  items: string[]
  translate: (translationKey: string) => string
  onSelect: (value: string) => void
}

export type TravelerCountSelectorProps = {
  value: number
  translate: (translationKey: string) => string
  onChange: (value: number) => void
}

export const ATTRACTIONS_PAGE_REGIONS = ['hero', 'search', 'advertising', 'filter', 'guest', 'results', 'auth'] as const

export type AttractionsPageRegion = (typeof ATTRACTIONS_PAGE_REGIONS)[number]

export const ATTRACTION_QUICK_DATE_PRESETS: AttractionQuickDatePreset[] = ['today', 'tomorrow', 'weekend', 'holiday']
export const ATTRACTION_HOT_SPOTS = ['上海 迪士尼', '杭州 西湖', '东京 迪士尼海洋', '北京 故宫']
export const ATTRACTION_RECENT_SEARCHES = ['上海 迪士尼乐园', '北京 环球影城', '杭州 灵隐寺']
