import type { HotelResponse } from './HotelResponse'

export type HotelListResponse = {
  hotels: HotelResponse[]
}
export const hotelListResponseFromJson = (json: string): HotelListResponse =>
  JSON.parse(json) as HotelListResponse

export const hotelListResponseToJson = (value: HotelListResponse): string =>
  JSON.stringify(value)
