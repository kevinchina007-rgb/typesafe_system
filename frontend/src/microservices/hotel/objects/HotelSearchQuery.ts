export type HotelSearchQuery = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
}
export const hotelSearchQueryFromJson = (json: string): HotelSearchQuery =>
  JSON.parse(json) as HotelSearchQuery

export const hotelSearchQueryToJson = (value: HotelSearchQuery): string =>
  JSON.stringify(value)
