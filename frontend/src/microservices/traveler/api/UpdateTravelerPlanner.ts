// 本文件是 traveler 模块的更新入口，直接承载请求组装、调用和返回类型定义。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UpdateTravelerPlannerRequest } from '@/microservices/traveler/objects/UpdateTravelerPlannerRequest'
import type { TravelerPlannerResponse } from '@/microservices/traveler/objects/TravelerPlannerResponse'

export const updateTraveler = (userId: string, travelerId: string, payload: UpdateTravelerPlannerRequest): Promise<TravelerPlannerResponse> =>
  executeJsonApiRequest('/UpdateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
    traveler: payload,
  })
