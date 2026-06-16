// 本文件定义 `CreateManagerFlightPlanner` 的前端请求对象，用于航司管理员创建航班。

import type { ManagerCabinPricingInput } from './ManagerCabinPricingInput'

export type CreateManagerFlightPlannerRequest = {
  managerId: string
  flightNumber: string
  departureAirport: string
  arrivalAirport: string
  departureTime: string
  arrivalTime: string
  economyCabin: ManagerCabinPricingInput
  premiumEconomyCabin: ManagerCabinPricingInput
  businessCabin: ManagerCabinPricingInput
  firstCabin: ManagerCabinPricingInput
  currency: string
}

export const createManagerFlightPlannerRequestFromJson = (json: string): CreateManagerFlightPlannerRequest =>
  JSON.parse(json) as CreateManagerFlightPlannerRequest

export const createManagerFlightPlannerRequestToJson = (value: CreateManagerFlightPlannerRequest): string =>
  JSON.stringify(value)
