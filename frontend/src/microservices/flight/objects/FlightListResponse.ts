import type { FlightResponse } from './FlightResponse'

export type FlightListPlannerResponse = {
  flights: FlightResponse[]
}

export type FlightListResponse = FlightListPlannerResponse

export const flightListResponseFromJson = (json: string): FlightListPlannerResponse =>
  JSON.parse(json) as FlightListPlannerResponse

export const flightListResponseToJson = (value: FlightListPlannerResponse): string =>
  JSON.stringify(value)
