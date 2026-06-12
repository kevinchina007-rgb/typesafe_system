// 本文件定义 ListManagerFlightsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { FlightListPlannerResponse } from '@/microservices/flight/objects/FlightListPlannerResponse'

export const listManagerFlights = (
  managerId: string,
  filters: {
    departureAirports?: string[]
    arrivalAirports?: string[]
    departureDate?: string
    timeRange?: string
    sortDirection?: 'asc' | 'desc'
  } = {},
): Promise<FlightListPlannerResponse> =>
  executeJsonApiRequest('/ListManagerFlightsPlanner', 'POST', { managerId, managerType: 'Airline', ...filters })
