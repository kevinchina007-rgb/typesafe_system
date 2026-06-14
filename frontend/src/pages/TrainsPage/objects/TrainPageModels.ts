import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse, TrainPlannerResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

// TrainsPage 使用的行程类型，只保留单程和往返两种。
export type TrainTripType = 'oneWay' | 'roundTrip'
// TrainsPage 使用的快捷日期预设，只保留页面上可点的几个固定值。
export type TrainQuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'nextWeek'
// TrainsPage 的座位偏好选项，只负责页面展示和请求传参。
export type TrainSeatPreference = 'Business' | 'FirstClass' | 'SecondClass' | 'SoftSleeper' | 'HardSleeper' | 'NoSeat'
// TrainsPage 的列车类型偏好，只负责页面展示和请求传参。
export type TrainTypePreference = 'HighSpeed' | 'Bullet' | 'Regular'
// TrainsPage 的排序方式，只保留结果列表需要的三种顺序。
export type TrainSortMode = 'highSpeedPriority' | 'lowPricePriority' | 'departureTimeEarly'

// TrainsPage 搜索过程中拆分出来的一段路线，用于结果区展示。
export type TrainSearchSegment = {
  fromStop: TrainPlannerResponse['stops'][number]
  toStop: TrainPlannerResponse['stops'][number]
  segmentStops: TrainPlannerResponse['stops']
}

// TrainsPage 的页面级参数，负责承接语言、用户和导航能力。
export type TrainsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: import('@/lib/mvp-types/index').AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

// TrainsPage 发送给后端的搜索请求对象，只包含页面会提交的条件。
export type TrainSearchRequest = {
  fromStation?: string
  toStation?: string
  date?: string
  returnDate?: string
  tripType?: TrainTripType
  passengerCount?: number
  seatPreference?: TrainSeatPreference
  trainTypePreference?: TrainTypePreference
}

// TrainsPage 的下单载荷，负责把列车、座位和出行人打包给预订流程。
export type TrainBookRequest = {
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
  orderCurrency: string
}

// TrainPage 顶部英雄区参数，只负责 eyebrow、标题和说明。
export type TrainPageHeroProps = {
  eyebrow: string
  title: string
  description: string
}

// TrainSearchCard 的参数，负责承接搜索表单状态和提交动作。
export type TrainSearchCardProps = {
  isBusy: boolean
  searchDate: string
  searchFromStation: string
  searchToStation: string
  translate: (translationKey: string) => string
  onSearchDateChange: (searchDate: string) => void
  onSearchFromStationChange: (searchFromStation: string) => void
  onSearchToStationChange: (searchToStation: string) => void
  onSearch: () => Promise<void>
}

// TrainFilterBar 的参数，只负责排序切换。
export type TrainFilterBarProps = {
  currentSortMode: TrainSortMode
  translate: (translationKey: string) => string
  onSortModeChange: (sortMode: TrainSortMode) => void
}

// TrainResultsSection 的参数，负责承接结果列表和预订动作。
export type TrainResultsSectionProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  searchFromStation: string
  searchToStation: string
  trainResponses: TrainPlannerResponse[]
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  translate: (translationKey: string) => string
  onToggleTravelerSelection: (travelerId: string) => void
  onRequireLogin: () => void
  onBookTrain: (payload: TrainBookRequest) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

// TrainsPage 控制器对页面暴露的完整状态和动作集合。
export type TrainsPageController = {
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  isBusy: boolean
  isGuestMode: boolean
  isTourGroupTargetMode: boolean
  targetTrainResponses: TrainPlannerResponse[]
  trainResponses: TrainPlannerResponse[]
  hasSearchedTrains: boolean
  searchRecommendation: string
  searchDate: string
  searchFromStation: string
  searchToStation: string
  trainSortMode: TrainSortMode
  dateWindowStart: string
  isAuthDialogOpen: boolean
  executeTrainSearch: () => Promise<void>
  onPreviousDateWindow: () => void
  onNextDateWindow: () => void
  handleDateSelect: (date: string) => Promise<void>
  setSearchDate: (value: string) => void
  setSearchFromStation: (value: string) => void
  setSearchToStation: (value: string) => void
  setTrainSortMode: (value: TrainSortMode) => void
  toggleTravelerSelection: (travelerId: string) => void
  onRequireLogin: () => void
  onAuthDialogClose: () => void
  onAuthDialogConfirm: () => void
  onBookTrain: (payload: TrainBookRequest) => Promise<void>
  loadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  loadReviewsByResource: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}
