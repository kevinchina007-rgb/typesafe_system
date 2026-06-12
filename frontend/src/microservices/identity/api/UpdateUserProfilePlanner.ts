// 本文件定义 UpdateUserProfilePlanner，负责 identity 模块的更新编排和接口入口。

import type { UserResponse } from '@/microservices/auth/objects/UserResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const updateUserProfile = (payload: { userId: string; nickname: string; phone: string }): Promise<UserResponse> =>
  executeJsonApiRequest('/UpdateUserProfilePlanner', 'POST', payload)
