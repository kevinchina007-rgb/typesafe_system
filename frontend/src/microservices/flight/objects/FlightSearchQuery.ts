export type FlightSearchQuery = {
  departureAirport?: string
  arrivalAirport?: string
  date?: string
}
export const flightSearchQueryFromJson = (json: string): FlightSearchQuery =>
  JSON.parse(json) as FlightSearchQuery

export const flightSearchQueryToJson = (value: FlightSearchQuery): string =>
  JSON.stringify(value)
