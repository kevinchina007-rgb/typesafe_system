export type FlightDailyLowestPricesRequest = {
  departureAirport: string
  arrivalAirport: string
  startDate: string
  days: number
  cabinClass?: string
}

export type FlightDailyLowestPriceResponse = {
  date: string
  lowestPrice: string | null
  currency: string | null
}

export type FlightDailyLowestPricesResponse = {
  prices: FlightDailyLowestPriceResponse[]
}

export const flightDailyLowestPricesRequestFromJson = (json: string): FlightDailyLowestPricesRequest =>
  JSON.parse(json) as FlightDailyLowestPricesRequest

export const flightDailyLowestPricesRequestToJson = (value: FlightDailyLowestPricesRequest): string =>
  JSON.stringify(value)
