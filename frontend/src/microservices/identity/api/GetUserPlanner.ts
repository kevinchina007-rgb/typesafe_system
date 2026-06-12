// 本文件定义 GetUserPlanner，负责 identity 模块的获取编排和接口入口。

import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getUser = (userId: string): Promise<UserResponse> =>
  executeJsonApiRequest('/GetUserPlanner', 'POST', { userId })
