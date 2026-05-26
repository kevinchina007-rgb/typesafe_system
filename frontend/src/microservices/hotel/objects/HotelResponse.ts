import type { RoomTypeSummaryResponse } from './RoomTypeSummaryResponse'

export type HotelResponse = {
  hotelId: string
  hotelName: string
  location: string
  status: string
  createdAt: string
  roomTypes: RoomTypeSummaryResponse[]
}
export const hotelResponseFromJson = (json: string): HotelResponse =>
  JSON.parse(json) as HotelResponse

export const hotelResponseToJson = (value: HotelResponse): string =>
  JSON.stringify(value)
