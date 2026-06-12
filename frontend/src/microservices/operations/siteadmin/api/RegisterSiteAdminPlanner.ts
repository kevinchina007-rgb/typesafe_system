// 本文件定义 RegisterSiteAdminPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'

export const registerSiteAdmin = (payload: { email: string; displayName: string; password: string }): Promise<CurrentManagerSessionResponse> =>
  executeJsonApiRequest('/RegisterSiteAdminPlanner', 'POST', payload)
