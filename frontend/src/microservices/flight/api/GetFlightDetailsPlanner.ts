// 本文件定义 GetFlightDetailsPlanner，负责 flight 模块的获取编排和接口入口。

import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'
import type { GetFlightDetailsPlannerRequest } from '@/microservices/flight/objects/GetFlightDetailsPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getFlightDetailsPlanner = (payload: GetFlightDetailsPlannerRequest): Promise<FlightPlannerResponse> =>
  executeJsonApiRequest('/GetFlightDetailsPlanner', 'POST', payload)
