// 本文件定义 CreateManagerRoomTypePlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerHotelPlannerResponse } from '@/microservices/operations/hotel/objects/ManagerHotelPlannerResponse'
import type { CreateManagerRoomTypePlannerRequest } from '@/microservices/operations/hotel/objects/CreateManagerRoomTypePlannerRequest'

export const createManagerRoomType = (payload: CreateManagerRoomTypePlannerRequest): Promise<ManagerHotelPlannerResponse> =>
  executeJsonApiRequest('/CreateManagerRoomTypePlanner', 'POST', payload)
