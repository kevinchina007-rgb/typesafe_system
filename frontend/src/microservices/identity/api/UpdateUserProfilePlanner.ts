// Identity UpdateUserProfilePlanner entry point.
import type { UpdateUserProfilePlannerRequest } from '@/microservices/identity/objects/UpdateUserProfilePlannerRequest'
import type { UserPlannerResponse } from '@/microservices/identity/objects/UserPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const updateUserProfilePlanner = (payload: UpdateUserProfilePlannerRequest): Promise<UserPlannerResponse> =>
  executeJsonApiRequest('/UpdateUserProfilePlanner', 'POST', payload)
