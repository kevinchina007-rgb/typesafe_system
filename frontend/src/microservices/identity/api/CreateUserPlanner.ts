// 本文件定义 CreateUserPlanner，负责 identity 模块的创建编排和接口入口。

import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createUser = (payload: { email: string; nickname: string; phone: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/CreateUserPlanner', 'POST', payload)
