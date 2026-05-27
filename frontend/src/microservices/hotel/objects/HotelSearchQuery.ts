export type HotelSearchPlannerRequest = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
}


export const hotelSearchQueryFromJson = (json: string): HotelSearchPlannerRequest =>
  JSON.parse(json) as HotelSearchPlannerRequest

export const hotelSearchQueryToJson = (value: HotelSearchPlannerRequest): string =>
  JSON.stringify(value)
