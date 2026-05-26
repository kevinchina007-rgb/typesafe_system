export type FlightSearchPlannerRequest = {
  departureAirport?: string
  arrivalAirport?: string
  date?: string
}

export type FlightSearchQuery = FlightSearchPlannerRequest

export const flightSearchQueryFromJson = (json: string): FlightSearchPlannerRequest =>
  JSON.parse(json) as FlightSearchPlannerRequest

export const flightSearchQueryToJson = (value: FlightSearchPlannerRequest): string =>
  JSON.stringify(value)
