// 本文件定义 LoginUserPlanner，负责 identity 模块的登录编排和接口入口。

import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const loginUser = (payload: { email: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/LoginUserPlanner', 'POST', payload)
