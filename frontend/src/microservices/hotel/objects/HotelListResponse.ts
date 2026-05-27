import type { HotelPlannerResponse } from './HotelPlannerResponse'

export type HotelListPlannerResponse = {
  hotels: HotelPlannerResponse[]
}


export const hotelListResponseFromJson = (json: string): HotelListPlannerResponse =>
  JSON.parse(json) as HotelListPlannerResponse

export const hotelListResponseToJson = (value: HotelListPlannerResponse): string =>
  JSON.stringify(value)
