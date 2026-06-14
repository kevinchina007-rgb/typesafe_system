import type { HotelPlannerResponse } from './HotelPlannerResponse'

export type HotelListPlannerResponse = {
  hotels: HotelPlannerResponse[]
}

export const hotelListPlannerResponseFromJson = (json: string): HotelListPlannerResponse =>
  JSON.parse(json) as HotelListPlannerResponse

export const hotelListPlannerResponseToJson = (value: HotelListPlannerResponse): string =>
  JSON.stringify(value)
