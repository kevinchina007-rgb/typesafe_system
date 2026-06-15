import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const payOrder = (orderId: string, payload: { paymentMethod: string; paymentSucceeded: boolean; travelerIds?: string[] }): Promise<OrderResponse> =>
  executeJsonApiRequest('/PayOrderPlanner', 'POST', { orderId, ...payload })
