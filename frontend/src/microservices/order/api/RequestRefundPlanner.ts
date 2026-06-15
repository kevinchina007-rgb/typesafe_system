// 本文件定义 `RequestRefundPlanner`，负责提交订单退款申请。

import type { RequestRefundPlannerRequest } from '@/microservices/order/objects/RequestRefundPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const requestRefund = (orderId: string, payload: { refundReason: string }): Promise<OrderResponse> => {
  const request: RequestRefundPlannerRequest = { orderId, ...payload }
  return executeJsonApiRequest('/RequestRefundPlanner', 'POST', request)
}
