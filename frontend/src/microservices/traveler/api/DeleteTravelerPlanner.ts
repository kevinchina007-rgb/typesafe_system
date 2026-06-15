// 本文件是 traveler 模块的删除入口，直接承载请求组装、调用和返回类型定义。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TravelerDeletedPlannerResponse } from '@/microservices/traveler/objects/TravelerDeletedPlannerResponse'

export const deleteTraveler = (userId: string, travelerId: string): Promise<TravelerDeletedPlannerResponse> =>
  executeJsonApiRequest('/DeleteTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
  })
