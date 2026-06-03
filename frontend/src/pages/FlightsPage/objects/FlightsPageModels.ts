import type { TravelerResponse } from '@/lib/mvp-types/index'
import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { AppLanguage, AppViewKey, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightPlannerRequest'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import type {
  FlightDailyLowestPricesPlannerRequest,
  FlightDailyLowestPricesPlannerResponse,
} from '@/microservices/flight/objects/FlightDailyLowestPrices'
import type { FlightResultGroup, FlightSearchSegment, FlightSearchState } from '@/app/stores/models/flights/flightTypes'

export type FlightsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export type FlightSortMode = 'price' | 'departureTime'

export type FlightResultsRoute = {
  departureAirport: string
  arrivalAirport: string
  departureDate: string
}

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
  initialSelectedCabin: string | null
}
