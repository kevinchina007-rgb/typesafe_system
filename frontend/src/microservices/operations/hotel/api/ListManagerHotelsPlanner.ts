// 本文件定义 ListManagerHotelsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerHotelListPlannerResponse } from '@/microservices/operations/hotel/objects/ManagerHotelListPlannerResponse'
import type { ManagerScopedPlannerRequest } from '@/microservices/operations/hotel/objects/ManagerScopedPlannerRequest'

export const listManagedHotels = (managerId: string): Promise<ManagerHotelListPlannerResponse> => {
  const payload: ManagerScopedPlannerRequest = { managerId, managerType: 'Hotel' }
  return executeJsonApiRequest<ManagerHotelListPlannerResponse>('/ListManagerHotelsPlanner', 'POST', payload)
}
