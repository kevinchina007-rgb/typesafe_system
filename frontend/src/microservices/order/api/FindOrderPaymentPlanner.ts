// 本文件定义 `FindOrderPaymentPlanner`，负责按订单和支付 ID 查询支付明细。

import type { FindOrderPaymentPlannerRequest } from '@/microservices/order/objects/FindOrderPaymentPlannerRequest'
import type { PaymentResponse } from '@/microservices/order/objects/PaymentResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const findOrderPayment = (orderId: string, paymentId: string): Promise<PaymentResponse> => {
  const request: FindOrderPaymentPlannerRequest = { orderId, paymentId }
  return executeJsonApiRequest('/FindOrderPaymentPlanner', 'POST', request)
}
