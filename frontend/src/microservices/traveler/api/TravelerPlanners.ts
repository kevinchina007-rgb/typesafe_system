import type { CreateTravelerRequest } from '@/microservices/traveler/objects/CreateTravelerRequest'
import type { UpdateTravelerRequest } from '@/microservices/traveler/objects/UpdateTravelerRequest'
import type { TravelerResponse } from '@/microservices/traveler/objects/TravelerResponse'
import type { TravelerListResponse } from '@/microservices/traveler/objects/TravelerListResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createTraveler = (userId: string, payload: CreateTravelerRequest): Promise<TravelerResponse> =>
  executeJsonApiRequest('/CreateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    traveler: payload,
  })

export const updateTraveler = (userId: string, travelerId: string, payload: UpdateTravelerRequest): Promise<TravelerResponse> =>
  executeJsonApiRequest('/UpdateTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
    traveler: payload,
  })

export const listTravelers = (userId: string): Promise<TravelerListResponse> =>
  executeJsonApiRequest('/ListTravelersPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
  })

export const deleteTraveler = (userId: string, travelerId: string): Promise<{ deleted: boolean; hidden: boolean }> =>
  executeJsonApiRequest('/DeleteTravelerPlanner', 'POST', {
    actingUserId: userId,
    ownerUserId: userId,
    travelerId,
  })
