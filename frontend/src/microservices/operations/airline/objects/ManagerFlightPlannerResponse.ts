// 本文件定义航空管理员航班详情响应结构。

import type { ManagerCabinInventoryPlannerResponse } from './ManagerCabinInventoryPlannerResponse'

export type ManagerFlightPlannerResponse = {
  flightId: string
  airlineId: string
  airlineName: string
  airlineCode: string
  airlineLogoPath?: string | null
  flightNumber: string
  aircraftModel?: string | null
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  status: string
  basePrice: string
  currency: string
  createdAt: string
  cabinInventories: ManagerCabinInventoryPlannerResponse[]
}

export const managerFlightPlannerResponseFromJson = (json: string): ManagerFlightPlannerResponse =>
  JSON.parse(json) as ManagerFlightPlannerResponse

export const managerFlightPlannerResponseToJson = (value: ManagerFlightPlannerResponse): string =>
  JSON.stringify(value)
