// 本文件定义 RegisterAirlineManagerPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { RegisterAirlineManagerPlannerRequest } from '@/microservices/operations/airline/objects/RegisterAirlineManagerPlannerRequest'
import type { AirlineManagerSessionPlannerResponse } from '@/microservices/operations/airline/objects/AirlineManagerSessionPlannerResponse'

export const registerAirlineManager = (payload: RegisterAirlineManagerPlannerRequest): Promise<AirlineManagerSessionPlannerResponse> =>
  executeJsonApiRequest<AirlineManagerSessionPlannerResponse>('/RegisterAirlineManagerPlanner', 'POST', payload)
