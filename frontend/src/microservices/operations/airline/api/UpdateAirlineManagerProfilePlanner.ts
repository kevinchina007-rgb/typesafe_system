// 本文件定义 UpdateAirlineManagerProfilePlanner，负责 operations 航空管理端的更新编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UpdateAirlineManagerProfilePlannerRequest } from '@/microservices/operations/airline/objects/UpdateAirlineManagerProfilePlannerRequest'
import type { AirlineManagerSessionPlannerResponse } from '@/microservices/operations/airline/objects/AirlineManagerSessionPlannerResponse'

export const updateAirlineManagerProfile = (payload: UpdateAirlineManagerProfilePlannerRequest): Promise<AirlineManagerSessionPlannerResponse> =>
  executeJsonApiRequest<AirlineManagerSessionPlannerResponse>('/UpdateAirlineManagerProfilePlanner', 'POST', payload)

