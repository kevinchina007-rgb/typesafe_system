import type { AppLanguage, AttractionResponse, ResourceReviewSummaryPlannerResponse, ReviewPlannerResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AppViewKey } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { AttractionQuickDatePreset, AttractionSortPreference, AttractionTypePreference } from '@/app/stores/models/attraction-booking-model'
import type { AttractionTicketTypeRuleResponse } from '@/lib/mvp-types/resources'

// AttractionsPage 的页面级输入参数，负责承接语言、用户和导航能力。
export type AttractionsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

// AttractionsPage 的搜索请求对象，只包含页面会发送到后端的筛选字段。
export type AttractionsSearchRequest = {
  city?: string
  keyword?: string
  useDate?: string
  sortPreference?: AttractionSortPreference
}

// AttractionsPage 的下单载荷，负责把景点、票种、日期和出行人打包给预订流程。
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

// AttractionsPage 的控制器对页面暴露的状态和动作集合。
export type AttractionsPageController = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  isTourGroupTargetMode: boolean
  targetAttractionResponses: AttractionResponse[]
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  focusedSessionId: string | null
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
  handleLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryPlannerResponse>
  handleLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewPlannerResponse[]>
  handleSelectQuickDatePreset: (preset: AttractionQuickDatePreset, nextDate: string) => void
  handleOpenAuthDialog: () => void
  handleCloseAuthDialog: () => void
  handleConfirmAuthDialog: () => void
}

// AttractionPage 顶部英雄区参数，只负责标题和说明。
export type AttractionPageHeroProps = {
  title: string
  description: string
}

// AttractionSearchCard 的参数，负责承接搜索表单和搜索建议。
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

// AttractionFilterBar 的参数，只承接筛选项和变更事件。
export type AttractionFilterBarProps = {
  hasSearchedAttractions: boolean
  sortPreference: AttractionSortPreference
  translate: (translationKey: string) => string
  onSortPreferenceChange: (value: AttractionSortPreference) => void
}

// AttractionResultsSection 的参数，负责承接结果列表和预订动作。
export type AttractionResultsSectionProps = {
  attractionResponses: AttractionResponse[]
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  focusedSessionId: string | null
  translate: (translationKey: string) => string
  useDateDraft: string
  onToggleTravelerSelection: (travelerId: string) => void
  onRequireLogin: () => void
  onBookAttraction: (payload: AttractionBookingPayload) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryPlannerResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewPlannerResponse[]>
}

// AttractionResultCard 的参数，负责单条景点结果的展示和操作。
export type AttractionResultCardProps = {
  attractionResponse: AttractionResponse
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  focusedSessionId: string | null
  translate: (translationKey: string) => string
  useDateDraft: string
  onRequireLogin: () => void
  onBookAttraction: (payload: AttractionBookingPayload) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryPlannerResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewPlannerResponse[]>
}

// AttractionTypeSelector 的参数，只负责切换景点类型筛选。
export type AttractionTypeSelectorProps = {
  value: AttractionTypePreference
  translate: (translationKey: string) => string
  onChange: (value: AttractionTypePreference) => void
}

// AttractionKeywordInput 的参数，只负责关键字输入。
export type AttractionKeywordInputProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

// CitySelector 的参数，只负责城市选择和建议项。
export type CitySelectorProps = {
  value: string
  translate: (translationKey: string) => string
  suggestions: string[]
  onChange: (value: string) => void
}

// DateSelector 的参数，只负责日期输入。
export type DateSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

// HotAttractions 的参数，只负责热门景点按钮列表。
export type HotAttractionsProps = {
  items: string[]
  translate: (translationKey: string) => string
  onSelect: (value: string) => void
}

// TravelerCountSelector 的参数，只负责出行人数选择。
export type TravelerCountSelectorProps = {
  value: number
  translate: (translationKey: string) => string
  onChange: (value: number) => void
}

// AttractionsPage 的分区顺序定义，只用于页面内部布局和调试。
export const ATTRACTIONS_PAGE_REGIONS = ['hero', 'search', 'advertising', 'filter', 'guest', 'results', 'auth'] as const

// AttractionsPage 的分区 key 联合类型，供页面布局复用。
export type AttractionsPageRegion = (typeof ATTRACTIONS_PAGE_REGIONS)[number]

// 景点页的快捷日期预设，只保留页面上可点击的几个固定入口。
export const ATTRACTION_QUICK_DATE_PRESETS: AttractionQuickDatePreset[] = ['today', 'tomorrow', 'weekend', 'holiday']
// 热门景点快捷词，只用于搜索卡片的快捷入口。
export const ATTRACTION_HOT_SPOTS = ['上海 迪士尼', '杭州 西湖', '东京 迪士尼海洋', '北京 故宫']
// 最近搜索词，只用于搜索卡片的辅助推荐。
export const ATTRACTION_RECENT_SEARCHES = ['上海 迪士尼乐园', '北京 环球影城', '杭州 灵隐寺']
