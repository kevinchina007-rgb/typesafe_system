export type BookFlightPlannerRequest = {
  userId: string
  flightId: string
  travelerIds: string[]
  cabinClass: string
}
export type BookFlightRequest = BookFlightPlannerRequest

export const bookFlightRequestFromJson = (json: string): BookFlightPlannerRequest =>
  JSON.parse(json) as BookFlightPlannerRequest

export const bookFlightRequestToJson = (value: BookFlightPlannerRequest): string =>
  JSON.stringify(value)
