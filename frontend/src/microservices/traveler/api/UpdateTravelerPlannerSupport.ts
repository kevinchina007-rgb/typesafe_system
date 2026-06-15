// 本文件直接保留 traveler“更新旅客”前端入口实现，避免再通过兼容壳转发。
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TravelerPlannerResponse } from '@/microservices/traveler/objects/TravelerPlannerResponse'
import type { UpdateTravelerPlannerRequest } from '@/microservices/traveler/objects/UpdateTravelerPlannerRequest'

export const updateTraveler = (userId: string, travelerId: string, payload: UpdateTravelerPlannerRequest): Promise<TravelerPlannerResponse> =>
  executeJsonApiRequest('/UpdateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
    traveler: payload,
  })
