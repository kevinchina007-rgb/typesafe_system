import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TravelerListResponse } from '@/microservices/traveler/objects/TravelerListResponse'

export const listTravelers = (userId: string): Promise<TravelerListResponse> =>
  executeJsonApiRequest('/ListTravelersPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
  })
