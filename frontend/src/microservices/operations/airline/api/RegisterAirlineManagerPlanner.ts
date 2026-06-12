// 本文件定义 RegisterAirlineManagerPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'

export const registerAirlineManager = (payload: {
  email: string
  displayName: string
  airlineName: string
  airlineCode: string
  password: string
}): Promise<ManagerSessionResponse> =>
  executeJsonApiRequest('/RegisterAirlineManagerPlanner', 'POST', payload)
