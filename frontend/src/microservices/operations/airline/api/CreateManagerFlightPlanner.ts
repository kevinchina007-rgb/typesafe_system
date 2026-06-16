// 本文件定义 CreateManagerFlightPlanner，负责 operations 航空管理端的创建编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateManagerFlightPlannerRequest } from '@/microservices/operations/airline/objects/CreateManagerFlightPlannerRequest'
import type { ManagerFlightPlannerResponse } from '@/microservices/operations/airline/objects/ManagerFlightPlannerResponse'

export const createManagerFlight = (payload: CreateManagerFlightPlannerRequest): Promise<ManagerFlightPlannerResponse> =>
  executeJsonApiRequest<ManagerFlightPlannerResponse>('/CreateManagerFlightPlanner', 'POST', payload)

