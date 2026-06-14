export type HotelBookingPlannerResponse = {
  orderId: string
  orderItemId: string
  status: string
  totalPriceAmount: string
  currency: string
}

export const hotelBookingPlannerResponseFromJson = (json: string): HotelBookingPlannerResponse =>
  JSON.parse(json) as HotelBookingPlannerResponse

export const hotelBookingPlannerResponseToJson = (value: HotelBookingPlannerResponse): string =>
  JSON.stringify(value)
