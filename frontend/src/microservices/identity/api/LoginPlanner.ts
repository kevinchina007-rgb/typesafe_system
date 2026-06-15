import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const loginUser = (payload: { email: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/IdentityLoginPlanner', 'POST', payload)
