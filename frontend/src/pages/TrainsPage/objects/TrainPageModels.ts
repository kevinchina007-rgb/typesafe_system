import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse, TrainResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

export type TrainTripType = 'oneWay' | 'roundTrip'
export type TrainQuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'nextWeek'
export type TrainSeatPreference = 'Business' | 'FirstClass' | 'SecondClass' | 'SoftSleeper' | 'HardSleeper' | 'NoSeat'
export type TrainTypePreference = 'HighSpeed' | 'Bullet' | 'Regular'
export type TrainSortMode = 'highSpeedPriority' | 'lowPricePriority' | 'departureTimeEarly'

export type TrainSearchSegment = {
  fromStop: TrainResponse['stops'][number]
  toStop: TrainResponse['stops'][number]
  segmentStops: TrainResponse['stops']
}

export type TrainsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: import('@/lib/mvp-types/index').AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

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

export type TrainBookRequest = {
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
  orderCurrency: string
}

export type TrainPageHeroProps = {
  eyebrow: string
  title: string
  description: string
}

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

export type TrainFilterBarProps = {
  currentSortMode: TrainSortMode
  translate: (translationKey: string) => string
  onSortModeChange: (sortMode: TrainSortMode) => void
}

export type TrainResultsSectionProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  searchFromStation: string
  searchToStation: string
  trainResponses: TrainResponse[]
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onBookTrain: (payload: TrainBookRequest) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export type TrainsPageController = {
  travelers: TravelerResponse[]
  isBusy: boolean
  isGuestMode: boolean
  trainResponses: TrainResponse[]
  hasSearchedTrains: boolean
  searchRecommendation: string
  searchDate: string
  searchFromStation: string
  searchToStation: string
  trainSortMode: TrainSortMode
  isAuthDialogOpen: boolean
  executeTrainSearch: () => Promise<void>
  setSearchDate: (value: string) => void
  setSearchFromStation: (value: string) => void
  setSearchToStation: (value: string) => void
  setTrainSortMode: (value: TrainSortMode) => void
  onRequireLogin: () => void
  onAuthDialogClose: () => void
  onAuthDialogConfirm: () => void
  onBookTrain: (payload: TrainBookRequest) => Promise<void>
  loadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  loadReviewsByResource: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}
