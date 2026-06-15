// 本文件定义 `SubmitOrderPlanner`，负责将订单推进到提交状态。

import type { OrderIdPlannerRequest } from '@/microservices/order/objects/OrderIdPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const submitOrder = (orderId: string): Promise<OrderResponse> => {
  const request: OrderIdPlannerRequest = { orderId }
  return executeJsonApiRequest('/SubmitOrderPlanner', 'POST', request)
}
