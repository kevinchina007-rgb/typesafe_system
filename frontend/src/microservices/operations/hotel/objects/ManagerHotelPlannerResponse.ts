import type { ManagerHotelRoomTypePlannerResponse } from './ManagerHotelRoomTypePlannerResponse'

export type ManagerHotelPlannerResponse = {
  hotelId: string
  hotelName: string
  location: string
  status: string
  createdAt: string
  roomTypes: ManagerHotelRoomTypePlannerResponse[]
}

export const managerHotelPlannerResponseFromJson = (json: string): ManagerHotelPlannerResponse =>
  JSON.parse(json) as ManagerHotelPlannerResponse

export const managerHotelPlannerResponseToJson = (value: ManagerHotelPlannerResponse): string =>
  JSON.stringify(value)
