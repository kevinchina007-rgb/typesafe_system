// 本文件定义 `GetOrderPlanner`，负责按订单 ID 获取订单详情。

import type { OrderIdPlannerRequest } from '@/microservices/order/objects/OrderIdPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getOrder = (orderId: string): Promise<OrderResponse> => {
  const request: OrderIdPlannerRequest = { orderId }
  return executeJsonApiRequest('/GetOrderPlanner', 'POST', request)
}
