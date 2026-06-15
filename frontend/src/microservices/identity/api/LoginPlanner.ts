// Identity login entry point, routed separately from auth login to avoid name collisions.
import type { LoginPlannerRequest } from '@/microservices/identity/objects/LoginPlannerRequest'
import type { UserPlannerResponse } from '@/microservices/identity/objects/UserPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const loginPlanner = (payload: LoginPlannerRequest): Promise<UserPlannerResponse> =>
  executeJsonApiRequest('/IdentityLoginPlanner', 'POST', payload)
