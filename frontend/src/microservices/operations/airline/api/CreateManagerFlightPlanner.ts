// 本文件定义 CreateManagerFlightPlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'
import type { ManagerCabinPricingInput } from '@/microservices/operations/objects/ManagerCabinPricingInput'

export const createManagerFlight = (payload: {
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
}): Promise<FlightPlannerResponse> =>
  executeJsonApiRequest('/CreateManagerFlightPlanner', 'POST', payload)
