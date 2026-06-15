// 本文件定义 `ListOrdersPlanner`，负责按用户列出订单。

import type { ListOrdersPlannerRequest } from '@/microservices/order/objects/ListOrdersPlannerRequest'
import type { OrderListResponse } from '@/microservices/order/objects/OrderListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listOrders = (userId: string): Promise<OrderListResponse> => {
  const request: ListOrdersPlannerRequest = { userId }
  return executeJsonApiRequest('/ListOrdersPlanner', 'POST', request)
}
