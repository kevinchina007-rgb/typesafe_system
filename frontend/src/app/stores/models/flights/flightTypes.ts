// 本文件定义航班相关枚举和类型集合，供航班页状态和筛选逻辑使用。

import type { FlightPlannerResponse, TravelerResponse } from '@/lib/mvp-types/index'
import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightPlannerRequest'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import type { FlightDailyLowestPricesPlannerRequest } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerRequest'
import type { FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerResponse'

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
