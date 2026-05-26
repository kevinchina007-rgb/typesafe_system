import type { FlightBookingWindowStatus } from './FlightBookingWindowStatus'
import type { CabinInventoryResponse } from './CabinInventoryResponse'

export type FlightResponse = {
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
export const flightResponseFromJson = (json: string): FlightResponse =>
  JSON.parse(json) as FlightResponse

export const flightResponseToJson = (value: FlightResponse): string =>
  JSON.stringify(value)
