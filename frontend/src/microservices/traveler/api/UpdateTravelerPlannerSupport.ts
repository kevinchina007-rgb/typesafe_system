import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { UpdateTravelerRequest } from '@/microservices/traveler/objects/UpdateTravelerRequest'
import type { TravelerResponse } from '@/microservices/traveler/objects/TravelerResponse'

export const updateTraveler = (userId: string, travelerId: string, payload: UpdateTravelerRequest): Promise<TravelerResponse> =>
  executeJsonApiRequest('/UpdateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
    traveler: payload,
  })
