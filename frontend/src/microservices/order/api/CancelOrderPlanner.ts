// 本文件定义 `CancelOrderPlanner`，负责取消订单。

import type { OrderIdPlannerRequest } from '@/microservices/order/objects/OrderIdPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const cancelOrder = (orderId: string): Promise<OrderResponse> => {
  const request: OrderIdPlannerRequest = { orderId }
  return executeJsonApiRequest('/CancelOrderPlanner', 'POST', request)
}
