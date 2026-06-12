// 本文件定义 ListManagerFlightOrdersPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerFlightOrderListResponse } from '@/microservices/operations/objects/ManagerFlightOrderListResponse'

export const listManagerFlightOrders = (managerId: string, flightId: string): Promise<ManagerFlightOrderListResponse> =>
  executeJsonApiRequest('/ListManagerFlightOrdersPlanner', 'POST', { managerId, flightId })
