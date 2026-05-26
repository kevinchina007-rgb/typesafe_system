export type FlightAirport = {
  airportCode: string
  cityName: string
  airportName: string
}

export const flightAirportFromJson = (json: string): FlightAirport =>
  JSON.parse(json) as FlightAirport

export const flightAirportToJson = (value: FlightAirport): string =>
  JSON.stringify(value)
