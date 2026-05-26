import type { FlightPlannerResponse, TravelerResponse } from '@/lib/mvp-types/index'
import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchQuery'
import type { FlightDailyLowestPricesPlannerRequest, FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPrices'

export type TripType = 'oneWay' | 'roundTrip' | 'multiCity'

export type FlightSearchSegment = {
  id: string
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  arrivalDate: string
}

export type FlightResultGroup = {
  id: string
  title: string
  subtitle: string
  flightResponses: FlightPlannerResponse[]
}

export type FlightSearchState = {
  tripType: TripType
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  multiCitySegments: FlightSearchSegment[]
  adults: number
  childrenCount: number
}

export type FlightsPanelProps = {
  isBusy: boolean
  isGuestMode: boolean
  signedInUserId: string | null
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesPlannerRequest) => Promise<FlightDailyLowestPricesPlannerResponse>
  onBookFlight: (payload: BookFlightPlannerRequest) => Promise<void>
  onValidationError: (message: string) => void
}
