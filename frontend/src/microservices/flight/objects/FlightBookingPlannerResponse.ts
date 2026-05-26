export type FlightBookingPlannerResponse = {
  orderId: string
  orderItemId: string
  status: string
  totalPriceAmount: string
  currency: string
}

export const flightBookingPlannerResponseFromJson = (json: string): FlightBookingPlannerResponse =>
  JSON.parse(json) as FlightBookingPlannerResponse

export const flightBookingPlannerResponseToJson = (value: FlightBookingPlannerResponse): string =>
  JSON.stringify(value)
