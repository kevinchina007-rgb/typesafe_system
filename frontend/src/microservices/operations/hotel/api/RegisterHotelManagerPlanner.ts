// 本文件定义 RegisterHotelManagerPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'

export const registerHotelManager = (payload: {
  email: string
  displayName: string
  hotelName: string
  location: string
  password: string
}): Promise<ManagerSessionResponse> =>
  executeJsonApiRequest('/RegisterHotelManagerPlanner', 'POST', payload)
