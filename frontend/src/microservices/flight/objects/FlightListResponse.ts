import type { FlightResponse } from './FlightResponse'

export type FlightListResponse = {
  flights: FlightResponse[]
}
export const flightListResponseFromJson = (json: string): FlightListResponse =>
  JSON.parse(json) as FlightListResponse

export const flightListResponseToJson = (value: FlightListResponse): string =>
  JSON.stringify(value)
