// 本文件定义 ToggleManagerFlightStatusPlanner，负责 operations 模块的切换编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'

export const toggleManagerFlightStatus = (payload: { managerId: string; flightId: string }): Promise<FlightPlannerResponse> =>
  executeJsonApiRequest('/ToggleManagerFlightStatusPlanner', 'POST', payload)
