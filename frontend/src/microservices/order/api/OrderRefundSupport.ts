import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const requestRefund = (orderId: string, payload: { refundReason: string }): Promise<OrderResponse> =>
  executeJsonApiRequest('/RequestRefundPlanner', 'POST', { orderId, ...payload })
