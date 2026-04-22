export type FlightBookingWindowStatus = 'Available' | 'SurchargeRequired' | 'Expired'

export type CabinInventoryResponse = {
  inventoryId: string
  cabinClass: string
  availableSeats: number
  unitPrice: string
  currency: string
  status: string
  isBookable: boolean
}

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

export type FlightListResponse = {
  flights: FlightResponse[]
}

export type FlightSearchQueryDto = {
  departureAirport?: string
  arrivalAirport?: string
  date?: string
}

export type BookFlightRequestDto = {
  flightId: string
  travelerIds: string[]
  cabinClass: string
}
