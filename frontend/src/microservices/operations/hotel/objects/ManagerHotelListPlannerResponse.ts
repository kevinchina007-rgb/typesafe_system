import type { ManagerHotelPlannerResponse } from './ManagerHotelPlannerResponse'

export type ManagerHotelListPlannerResponse = {
  hotels: ManagerHotelPlannerResponse[]
}

export const managerHotelListPlannerResponseFromJson = (json: string): ManagerHotelListPlannerResponse =>
  JSON.parse(json) as ManagerHotelListPlannerResponse

export const managerHotelListPlannerResponseToJson = (value: ManagerHotelListPlannerResponse): string =>
  JSON.stringify(value)
