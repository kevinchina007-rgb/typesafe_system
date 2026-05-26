import type { FlightAirport } from './FlightAirport'

export type FlightCity = {
  cityName: string
  airports: FlightAirport[]
}

export const flightCityFromJson = (json: string): FlightCity =>
  JSON.parse(json) as FlightCity

export const flightCityToJson = (value: FlightCity): string =>
  JSON.stringify(value)
