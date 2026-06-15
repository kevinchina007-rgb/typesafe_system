import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const requestRefund = (orderId: string, payload: { refundReason: string }): Promise<OrderResponse> =>
  executeJsonApiRequest('/RequestRefundPlanner', 'POST', { orderId, ...payload })
