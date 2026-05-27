import type { FlightPlannerResponse } from './FlightPlannerResponse'

export type FlightListPlannerResponse = {
  flights: FlightPlannerResponse[]
}


export const flightListResponseFromJson = (json: string): FlightListPlannerResponse =>
  JSON.parse(json) as FlightListPlannerResponse

export const flightListResponseToJson = (value: FlightListPlannerResponse): string =>
  JSON.stringify(value)
