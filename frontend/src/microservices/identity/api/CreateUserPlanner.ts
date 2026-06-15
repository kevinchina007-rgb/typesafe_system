// Identity CreateUserPlanner entry point.
import type { CreateUserPlannerRequest } from '@/microservices/identity/objects/CreateUserPlannerRequest'
import type { UserPlannerResponse } from '@/microservices/identity/objects/UserPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createUserPlanner = (payload: CreateUserPlannerRequest): Promise<UserPlannerResponse> =>
  executeJsonApiRequest('/CreateUserPlanner', 'POST', payload)
