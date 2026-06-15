// 本文件定义 `ApproveRefundPlanner`，负责审批订单退款。

import type { RefundDecisionPlannerRequest } from '@/microservices/order/objects/RefundDecisionPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const approveOrderRefund = (orderId: string, refundId: string): Promise<OrderResponse> => {
  const request: RefundDecisionPlannerRequest = { orderId, refundId }
  return executeJsonApiRequest('/ApproveRefundPlanner', 'POST', request)
}
