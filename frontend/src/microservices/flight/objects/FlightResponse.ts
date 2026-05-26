import type { FlightBookingWindowStatus } from './FlightBookingWindowStatus'
import type { CabinInventoryResponse } from './CabinInventoryResponse'

export type FlightPlannerResponse = {
  flightId: string
  airlineId: string
  airlineName: string
  airlineCode: string
  airlineLogoPath: string | null
  flightNumber: string
  aircraftModel: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  status: string
  bookingWindowStatus: FlightBookingWindowStatus
  canBookOnline: boolean
  bookingNotice: string | null
  lateBookingSurchargeAmount: string | null
  lateBookingSurchargeCurrency: string | null
  basePrice: string
  currency: string
  createdAt: string
  cabinInventories: CabinInventoryResponse[]
}

export type FlightResponse = FlightPlannerResponse

export const flightResponseFromJson = (json: string): FlightPlannerResponse =>
  JSON.parse(json) as FlightPlannerResponse

export const flightResponseToJson = (value: FlightPlannerResponse): string =>
  JSON.stringify(value)
