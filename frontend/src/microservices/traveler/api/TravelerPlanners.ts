import type { CreateTravelerRequest } from '@/microservices/traveler/objects/CreateTravelerRequest'
import type { UpdateTravelerRequest } from '@/microservices/traveler/objects/UpdateTravelerRequest'
import type { TravelerResponse } from '@/microservices/traveler/objects/TravelerResponse'
import type { TravelerListResponse } from '@/microservices/traveler/objects/TravelerListResponse'
import { executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createTraveler = (userId: string, payload: CreateTravelerRequest): Promise<TravelerResponse> =>
    executeJsonApiRequest(`/users/${userId}/travelers`, 'POST', payload)

export const updateTraveler = (userId: string, travelerId: string, payload: UpdateTravelerRequest): Promise<TravelerResponse> =>
    executeJsonApiRequest(`/users/${userId}/travelers/${travelerId}`, 'PUT', payload)

export const listTravelers = (userId: string): Promise<TravelerListResponse> =>
    executeApiRequest(`/users/${userId}/travelers`)

export const deleteTraveler = (userId: string, travelerId: string): Promise<void> =>
    executeApiRequest(`/users/${userId}/travelers/${travelerId}`, { method: 'DELETE' })
