// 本文件直接保留 traveler“创建旅客”前端入口实现，避免再通过兼容壳转发。
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateTravelerPlannerRequest } from '@/microservices/traveler/objects/CreateTravelerPlannerRequest'
import type { TravelerPlannerResponse } from '@/microservices/traveler/objects/TravelerPlannerResponse'

export const createTraveler = (userId: string, payload: CreateTravelerPlannerRequest): Promise<TravelerPlannerResponse> =>
  executeJsonApiRequest('/CreateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    traveler: payload,
  })
