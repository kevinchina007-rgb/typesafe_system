// 本文件直接保留 traveler“删除旅客”前端入口实现，避免再通过兼容壳转发。
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TravelerDeletedPlannerResponse } from '@/microservices/traveler/objects/TravelerDeletedPlannerResponse'

export const deleteTraveler = (userId: string, travelerId: string): Promise<TravelerDeletedPlannerResponse> =>
  executeJsonApiRequest('/DeleteTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
  })
