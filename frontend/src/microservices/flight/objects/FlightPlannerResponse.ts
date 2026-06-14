// 本文件定义 flight 模块的 `FlightPlannerResponse`，作为 planner 响应数据并提供 JSON 编解码。

import type { FlightBookingWindowStatus } from "./FlightBookingWindowStatus"
import type { CabinInventoryResponse } from "./CabinInventoryResponse"

export type FlightPlannerResponse = {
  flightId: string
  airlineId: string
  airlineName: string
  airlineCode: string
  airlineLogoPath: string | null
  flightNumber: string
  aircraftModel: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  status: string
  bookingWindowStatus: FlightBookingWindowStatus
  canBookOnline: boolean
  bookingNotice: string | null
  lateBookingSurchargeAmount: string | null
  lateBookingSurchargeCurrency: string | null
  basePrice: string
  currency: string
  createdAt: string
  cabinInventories: CabinInventoryResponse[]
}

export const flightPlannerResponseFromJson = (json: string): FlightPlannerResponse =>
  JSON.parse(json) as FlightPlannerResponse

export const flightPlannerResponseToJson = (value: FlightPlannerResponse): string =>
  JSON.stringify(value)