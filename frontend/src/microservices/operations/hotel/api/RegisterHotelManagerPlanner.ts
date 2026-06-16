// 本文件定义 RegisterHotelManagerPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { RegisterHotelManagerPlannerRequest } from '@/microservices/operations/hotel/objects/RegisterHotelManagerPlannerRequest'
import type { HotelManagerSessionPlannerResponse } from '@/microservices/operations/hotel/objects/HotelManagerSessionPlannerResponse'

export const registerHotelManager = (payload: RegisterHotelManagerPlannerRequest): Promise<HotelManagerSessionPlannerResponse> =>
  executeJsonApiRequest<HotelManagerSessionPlannerResponse>('/RegisterHotelManagerPlanner', 'POST', payload)
