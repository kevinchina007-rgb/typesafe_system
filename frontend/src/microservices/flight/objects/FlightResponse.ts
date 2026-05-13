import type { FlightBookingWindowStatus } from './FlightBookingWindowStatus'
import type { CabinInventoryResponse } from './CabinInventoryResponse'

export type FlightResponse = {
  flightId: string
  airlineId: string
  airlineName: string
  airlineCode: string
  flightNumber: string
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
