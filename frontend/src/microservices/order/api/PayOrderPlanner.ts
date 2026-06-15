// 本文件定义 `PayOrderPlanner`，负责提交订单支付动作。

import type { PayOrderPlannerRequest } from '@/microservices/order/objects/PayOrderPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const payOrder = (orderId: string, payload: { paymentMethod: string; paymentSucceeded: boolean; travelerIds?: string[] }): Promise<OrderResponse> => {
  const request: PayOrderPlannerRequest = { orderId, ...payload }
  return executeJsonApiRequest('/PayOrderPlanner', 'POST', request)
}
