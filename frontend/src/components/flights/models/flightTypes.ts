import type {
  AppLanguage,
  FlightResponse,
  ResourceReviewSummaryResponse,
  ReviewResponse,
  TravelerResponse,
} from '../../../lib/mvp-types'
import type { BookFlightRequestDto, FlightSearchQueryDto } from '../../../lib/api-dtos/flights'
import type { TripType } from '../controls/TripTypeSelector'

export type QuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'nextWeek'

export type FlightSearchSegment = {
  id: string
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  arrivalDate: string
}

export type FlightSearchState = {
  tripType: TripType
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  selectedQuickDatePreset: QuickDatePreset | null
  multiCitySegments: FlightSearchSegment[]
  adults: number
  childrenCount: number
  cabinPreference: string
}

export type FlightsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onSearchFlights: (payload: FlightSearchQueryDto) => Promise<FlightResponse[]>
  onBookFlight: (payload: BookFlightRequestDto) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}
