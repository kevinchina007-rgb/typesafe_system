import type { FlightResponse, TravelerResponse } from '@/lib/mvp-types/index'
import type { BookFlightRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { FlightSearchQuery } from '@/microservices/flight/objects/FlightSearchQuery'
import type { FlightDailyLowestPricesRequest, FlightDailyLowestPricesResponse } from '@/microservices/flight/objects/FlightDailyLowestPrices'

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
  flightResponses: FlightResponse[]
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
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onSearchFlights: (payload: FlightSearchQuery) => Promise<FlightResponse[]>
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesRequest) => Promise<FlightDailyLowestPricesResponse>
  onBookFlight: (payload: BookFlightRequest) => Promise<void>
  onValidationError: (message: string) => void
}
