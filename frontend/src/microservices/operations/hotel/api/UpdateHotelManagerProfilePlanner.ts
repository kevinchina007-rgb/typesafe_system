// 本文件定义 UpdateHotelManagerProfilePlanner，负责 operations 模块的更新编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'
import type { UpdateHotelManagerProfilePlannerRequest } from '@/microservices/operations/objects/UpdateHotelManagerProfilePlannerRequest'

export const updateHotelManagerProfile = (payload: UpdateHotelManagerProfilePlannerRequest): Promise<ManagerSessionResponse> =>
  executeJsonApiRequest('/UpdateHotelManagerProfilePlanner', 'POST', payload)
