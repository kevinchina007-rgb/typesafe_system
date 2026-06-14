import type { TravelerResponse } from '@/lib/mvp-types/index'
import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { AppLanguage, AppViewKey, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightPlannerRequest'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import type { FlightDailyLowestPricesPlannerRequest } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerRequest'
import type { FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerResponse'
import type { FlightResultGroup, FlightSearchSegment, FlightSearchState } from '@/app/stores/models/flights/flightTypes'

// FlightsPage 顶层参数，负责把语言、登录用户和导航能力传给页面。
export type FlightsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

// FlightsPage 的排序方式，只保留页面需要的两种展示顺序。
export type FlightSortMode = 'price' | 'departureTime'

// FlightsPage 当前展示的一条航线，包含起降机场和日期。
export type FlightResultsRoute = {
  departureAirport: string
  arrivalAirport: string
  departureDate: string
}

// FlightsPage 中单条航班的展示数据，负责把原始响应整理成视图可直接使用的结构。
export type DisplayFlight = {
  flight: FlightPlannerResponse
  airlineName: string
  airlineLogoPath: string | null
  departureAirportName: string
  arrivalAirportName: string
  displayCabinClass: string
  displayCabinLabel: string
  displayPrice: number
  displayCurrency: string
  isDisplayCabinBookable: boolean
  priceTone: 'lowest' | 'discount' | 'standard'
}

// FlightSearchCard 参数，负责承接搜索面板里的所有表单状态和事件。
export type FlightSearchCardProps = {
  tripType: 'oneWay' | 'roundTrip' | 'multiCity'
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  multiCitySegments: FlightSearchSegment[]
  translate: (translationKey: string) => string
  onTripTypeChange: (value: 'oneWay' | 'roundTrip' | 'multiCity') => void
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onDepartureDateChange: (value: string) => void
  onReturnDateChange: (value: string) => void
  onMultiCitySegmentChange: (
    segmentId: string,
    key: 'departureAirport' | 'arrivalAirport' | 'departureDate' | 'arrivalDate',
    value: string,
  ) => void
  onAddMultiCitySegment: () => void
  onRemoveMultiCitySegment: (segmentId: string) => void
  onSwapRoute: () => void
  showSubmitButton: boolean
  onSubmit: () => void
}

// FlightResultsSection 参数，负责承接结果区的查询状态、筛选条件和预订动作。
export type FlightResultsSectionProps = {
  searchState: FlightSearchState
  flightResponses: FlightPlannerResponse[]
  flightResultGroups: FlightResultGroup[]
  hasSearchedFlights: boolean
  isBusy: boolean
  isGuestMode: boolean
  signedInUserId: string | null
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  onToggleTravelerSelection: (travelerId: string) => void
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onBookFlight: (payload: BookFlightPlannerRequest) => Promise<void>
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesPlannerRequest) => Promise<FlightDailyLowestPricesPlannerResponse>
  onDepartureDateChange: (value: string) => void
  onReturnDateChange: (value: string) => void
  onMultiCitySegmentChange: (
    segmentId: string,
    key: 'departureAirport' | 'arrivalAirport' | 'departureDate' | 'arrivalDate',
    value: string,
  ) => void
  onRequireLateBookingReview: (flightResponse: FlightPlannerResponse) => void
  getLateBookingNotice: (flightResponse: FlightPlannerResponse) => string
  initialSelectedCabin?: string | null
}

// FlightsPage controller 暴露给视图层的状态和动作集合。
export type FlightsPageController = {
  travelers: TravelerResponse[]
  isBusy: boolean
  isAuthDialogOpen: boolean
  lateBookingFlight: FlightPlannerResponse | null
  signedInUserId: string | null
  isGuestMode: boolean
  isTourGroupTargetMode: boolean
  targetFlightResponses: FlightPlannerResponse[]
  targetFlightResultGroups: FlightResultGroup[]
  deliveryAdvertisements: AdvertisementResponse[]
  openAuthDialog: () => void
  closeAuthDialog: () => void
  openLateBookingReview: (flightResponse: FlightPlannerResponse) => void
  closeLateBookingReview: () => void
  submitSearch: () => Promise<void>
  searchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>
  loadDailyLowestPrices: (payload: FlightDailyLowestPricesPlannerRequest) => Promise<FlightDailyLowestPricesPlannerResponse>
  bookFlight: (payload: BookFlightPlannerRequest) => Promise<void>
  selectedTravelerIds: string[]
  toggleTravelerSelection: (travelerId: string) => void
  handleOpenAdvertisement: (advertisement: AdvertisementResponse) => void
  initialSelectedCabin: string | null
}
