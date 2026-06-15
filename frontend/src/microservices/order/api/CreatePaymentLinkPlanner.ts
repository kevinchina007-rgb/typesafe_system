// 本文件定义 `CreatePaymentLinkPlanner`，负责生成订单支付链接。

import type { CreatePaymentLinkPlannerRequest } from '@/microservices/order/objects/CreatePaymentLinkPlannerRequest'
import type { PaymentLinkResponse } from '@/microservices/order/objects/PaymentLinkResponse'
import { getTravelBackendOrigin } from '@/lib/config/runtime-config'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createPaymentLink = (orderId: string, userId: string, paymentMethod: string, language: 'zh'): Promise<PaymentLinkResponse> => {
  const request: CreatePaymentLinkPlannerRequest = {
    orderId,
    userId,
    paymentMethod,
    language,
    publicBackendOrigin: getTravelBackendOrigin(),
  }
  return executeJsonApiRequest('/CreatePaymentLinkPlanner', 'POST', request)
}
