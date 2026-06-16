// 本文件定义 ListManagerFlightOrdersPlanner，负责 operations 航空管理端的订单列表查询和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerFlightOrderListResponse } from '@/microservices/operations/airline/objects/ManagerFlightOrderListResponse'
import type { ManagerFlightOrdersPlannerRequest } from '@/microservices/operations/airline/objects/ManagerFlightOrdersPlannerRequest'

export const listManagerFlightOrders = (payload: ManagerFlightOrdersPlannerRequest): Promise<ManagerFlightOrderListResponse> =>
  executeJsonApiRequest<ManagerFlightOrderListResponse>('/ListManagerFlightOrdersPlanner', 'POST', payload)

