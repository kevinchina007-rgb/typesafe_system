// 本文件定义 UpdateAirlineManagerProfilePlanner，负责 operations 模块的更新编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'
import type { UpdateAirlineManagerProfilePlannerRequest } from '@/microservices/operations/objects/UpdateAirlineManagerProfilePlannerRequest'

export const updateAirlineManagerProfile = (payload: UpdateAirlineManagerProfilePlannerRequest): Promise<ManagerSessionResponse> =>
  executeJsonApiRequest('/UpdateAirlineManagerProfilePlanner', 'POST', payload)
