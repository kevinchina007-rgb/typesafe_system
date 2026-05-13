import type { AppLanguage, FlightResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import type { BookFlightRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { FlightSearchQuery } from '@/microservices/flight/objects/FlightSearchQuery'

export type TripType = 'oneWay' | 'roundTrip' | 'multiCity'
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
  onSearchFlights: (payload: FlightSearchQuery) => Promise<FlightResponse[]>
  onBookFlight: (payload: BookFlightRequest) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}
