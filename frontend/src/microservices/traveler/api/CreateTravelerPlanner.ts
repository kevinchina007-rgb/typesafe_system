// 本文件是 traveler 模块的创建入口，直接承载请求组装、调用和返回类型定义。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateTravelerPlannerRequest } from '@/microservices/traveler/objects/CreateTravelerPlannerRequest'
import type { TravelerPlannerResponse } from '@/microservices/traveler/objects/TravelerPlannerResponse'

export const createTraveler = (userId: string, payload: CreateTravelerPlannerRequest): Promise<TravelerPlannerResponse> =>
  executeJsonApiRequest('/CreateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    traveler: payload,
  })
