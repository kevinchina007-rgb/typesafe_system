// 本文件定义 ToggleManagerFlightStatusPlanner，负责 operations 模块的切换编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerFlightPlannerResponse } from '@/microservices/operations/airline/objects/ManagerFlightPlannerResponse'
import type { ToggleManagerFlightStatusPlannerRequest } from '@/microservices/operations/airline/objects/ToggleManagerFlightStatusPlannerRequest'

export const toggleManagerFlightStatus = (payload: ToggleManagerFlightStatusPlannerRequest): Promise<ManagerFlightPlannerResponse> =>
  executeJsonApiRequest<ManagerFlightPlannerResponse>('/ToggleManagerFlightStatusPlanner', 'POST', payload)
