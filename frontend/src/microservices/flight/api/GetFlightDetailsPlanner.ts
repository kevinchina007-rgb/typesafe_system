// 本文件定义 GetFlightDetailsPlanner，负责 flight 模块的获取编排和接口入口。

import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getFlightDetailsPlanner = (flightId: string): Promise<FlightPlannerResponse> =>
  executeJsonApiRequest('/GetFlightDetailsPlanner', 'POST', { flightId })
