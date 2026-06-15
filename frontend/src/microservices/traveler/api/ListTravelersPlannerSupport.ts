// 本文件直接保留 traveler“列出旅客”前端入口实现，避免再通过兼容壳转发。
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ListTravelersPlannerRequest } from '@/microservices/traveler/objects/ListTravelersPlannerRequest'
import type { TravelerListPlannerResponse } from '@/microservices/traveler/objects/TravelerListPlannerResponse'

export const listTravelers = (userId: string): Promise<TravelerListPlannerResponse> =>
  executeJsonApiRequest('/ListTravelersPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
  } satisfies ListTravelersPlannerRequest)
