export type FlightDailyLowestPricesPlannerRequest = {
  departureAirport: string
  arrivalAirport: string
  startDate: string
  days: number
  cabinClass?: string
}

export type FlightDailyLowestPricesRequest = FlightDailyLowestPricesPlannerRequest

export type FlightDailyLowestPricePlannerResponse = {
  date: string
  lowestPrice: string | null
  currency: string | null
}

export type FlightDailyLowestPriceResponse = FlightDailyLowestPricePlannerResponse

export type FlightDailyLowestPricesPlannerResponse = {
  prices: FlightDailyLowestPricePlannerResponse[]
}

export type FlightDailyLowestPricesResponse = FlightDailyLowestPricesPlannerResponse

export const flightDailyLowestPricesRequestFromJson = (json: string): FlightDailyLowestPricesPlannerRequest =>
  JSON.parse(json) as FlightDailyLowestPricesPlannerRequest

export const flightDailyLowestPricesRequestToJson = (value: FlightDailyLowestPricesPlannerRequest): string =>
  JSON.stringify(value)
