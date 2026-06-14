export type HotelSearchPlannerRequest = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
}

export const hotelSearchPlannerRequestFromJson = (json: string): HotelSearchPlannerRequest =>
  JSON.parse(json) as HotelSearchPlannerRequest

export const hotelSearchPlannerRequestToJson = (value: HotelSearchPlannerRequest): string =>
  JSON.stringify(value)
