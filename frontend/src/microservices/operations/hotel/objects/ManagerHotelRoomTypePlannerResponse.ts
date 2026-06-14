import type { RoomTypeSummaryResponse } from '@/microservices/hotel/objects/RoomTypeSummaryResponse'

export type ManagerHotelRoomTypePlannerResponse = RoomTypeSummaryResponse

export const managerHotelRoomTypePlannerResponseFromJson = (json: string): ManagerHotelRoomTypePlannerResponse =>
  JSON.parse(json) as ManagerHotelRoomTypePlannerResponse

export const managerHotelRoomTypePlannerResponseToJson = (value: ManagerHotelRoomTypePlannerResponse): string =>
  JSON.stringify(value)
