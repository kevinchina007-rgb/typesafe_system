// 本文件是 traveler 模块的列表入口，直接承载请求组装、调用和返回类型定义。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ListTravelersPlannerRequest } from '@/microservices/traveler/objects/ListTravelersPlannerRequest'
import type { TravelerListPlannerResponse } from '@/microservices/traveler/objects/TravelerListPlannerResponse'

export const listTravelers = (userId: string): Promise<TravelerListPlannerResponse> =>
  executeJsonApiRequest('/ListTravelersPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
  } satisfies ListTravelersPlannerRequest)
