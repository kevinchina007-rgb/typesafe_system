// 本文件定义 `SettleRefundPlanner`，负责结算已审批的退款。

import type { RefundDecisionPlannerRequest } from '@/microservices/order/objects/RefundDecisionPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const settleRefund = (orderId: string, refundId: string): Promise<OrderResponse> => {
  const request: RefundDecisionPlannerRequest = { orderId, refundId }
  return executeJsonApiRequest('/SettleRefundPlanner', 'POST', request)
}
