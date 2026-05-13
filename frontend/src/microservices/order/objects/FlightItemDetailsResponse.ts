export type FlightItemDetailsResponse = {
  airlineName: string
  airlineCode: string
  flightId: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  cabinClass: string
  travelerIds: string[]
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  currency: string
}
