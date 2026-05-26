export type BookFlightRequest = {
  userId?: string
  flightId: string
  travelerIds: string[]
  cabinClass: string
}
export const bookFlightRequestFromJson = (json: string): BookFlightRequest =>
  JSON.parse(json) as BookFlightRequest

export const bookFlightRequestToJson = (value: BookFlightRequest): string =>
  JSON.stringify(value)
