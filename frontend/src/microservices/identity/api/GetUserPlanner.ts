// Identity GetUserPlanner entry point.
import type { GetUserPlannerRequest } from '@/microservices/identity/objects/GetUserPlannerRequest'
import type { UserPlannerResponse } from '@/microservices/identity/objects/UserPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getUserPlanner = (payload: GetUserPlannerRequest): Promise<UserPlannerResponse> =>
  executeJsonApiRequest('/GetUserPlanner', 'POST', payload)
