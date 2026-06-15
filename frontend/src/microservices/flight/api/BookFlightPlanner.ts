// 本文件定义 BookFlightPlanner，负责 flight 模块的预订编排和接口入口。

import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightPlannerRequest'
import type { FlightBookingPlannerResponse } from '@/microservices/flight/objects/FlightBookingPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const bookFlightPlanner = (payload: BookFlightPlannerRequest): Promise<FlightBookingPlannerResponse> =>
  executeJsonApiRequest('/BookFlightPlanner', 'POST', payload)
