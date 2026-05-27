export type FlightDailyLowestPricesPlannerRequest = {
  departureAirport: string
  arrivalAirport: string
  startDate: string
  days: number
  cabinClass?: string
}


export type FlightDailyLowestPricePlannerResponse = {
  date: string
  lowestPrice: string | null
  currency: string | null
}


export type FlightDailyLowestPricesPlannerResponse = {
  prices: FlightDailyLowestPricePlannerResponse[]
}


export const flightDailyLowestPricesRequestFromJson = (json: string): FlightDailyLowestPricesPlannerRequest =>
  JSON.parse(json) as FlightDailyLowestPricesPlannerRequest

export const flightDailyLowestPricesRequestToJson = (value: FlightDailyLowestPricesPlannerRequest): string =>
  JSON.stringify(value)
