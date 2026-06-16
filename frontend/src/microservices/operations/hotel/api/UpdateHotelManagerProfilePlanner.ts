// 本文件定义 UpdateHotelManagerProfilePlanner，负责 operations 模块的更新编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UpdateHotelManagerProfilePlannerRequest } from '@/microservices/operations/hotel/objects/UpdateHotelManagerProfilePlannerRequest'
import type { HotelManagerSessionPlannerResponse } from '@/microservices/operations/hotel/objects/HotelManagerSessionPlannerResponse'

export const updateHotelManagerProfile = (payload: UpdateHotelManagerProfilePlannerRequest): Promise<HotelManagerSessionPlannerResponse> =>
  executeJsonApiRequest<HotelManagerSessionPlannerResponse>('/UpdateHotelManagerProfilePlanner', 'POST', payload)
