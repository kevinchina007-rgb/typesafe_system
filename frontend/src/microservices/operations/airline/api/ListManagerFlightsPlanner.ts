// 本文件定义 ListManagerFlightsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerFlightListPlannerResponse } from '@/microservices/operations/airline/objects/ManagerFlightListPlannerResponse'
import type { ManagerFlightsPlannerRequest } from '@/microservices/operations/airline/objects/ManagerFlightsPlannerRequest'

export const listManagerFlights = (payload: ManagerFlightsPlannerRequest): Promise<ManagerFlightListPlannerResponse> =>
  executeJsonApiRequest<ManagerFlightListPlannerResponse>('/ListManagerFlightsPlanner', 'POST', payload)
