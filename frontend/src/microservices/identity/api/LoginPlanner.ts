import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const loginUser = (payload: { email: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/LoginPlanner', 'POST', payload)
