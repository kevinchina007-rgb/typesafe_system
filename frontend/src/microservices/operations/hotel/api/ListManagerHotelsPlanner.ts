// 本文件定义 ListManagerHotelsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { HotelListPlannerResponse } from '@/microservices/hotel/objects/HotelListPlannerResponse'

export const listManagedHotels = (managerId: string): Promise<HotelListPlannerResponse> =>
  executeJsonApiRequest('/ListManagerHotelsPlanner', 'POST', { managerId, managerType: 'Hotel' })
