import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { CreateTravelerRequest } from '@/microservices/traveler/objects/CreateTravelerRequest'
import type { TravelerResponse } from '@/microservices/traveler/objects/TravelerResponse'

export const createTraveler = (userId: string, payload: CreateTravelerRequest): Promise<TravelerResponse> =>
  executeJsonApiRequest('/CreateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    traveler: payload,
  })
