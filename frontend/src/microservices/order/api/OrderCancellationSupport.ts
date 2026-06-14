import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const cancelOrder = (orderId: string): Promise<OrderResponse> =>
  executeJsonApiRequest('/CancelOrderPlanner', 'POST', { orderId })
