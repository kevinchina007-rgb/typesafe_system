import type { OrderListResponse } from '@/microservices/order/objects/OrderListResponse'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createOrder = (payload: { ownerUserId: string; orderCurrency: string }): Promise<OrderResponse> =>
  executeJsonApiRequest('/CreateOrderPlanner', 'POST', payload)

export const getOrder = (orderId: string): Promise<OrderResponse> =>
  executeJsonApiRequest('/GetOrderPlanner', 'POST', { orderId })

export const listOrders = (userId: string): Promise<OrderListResponse> =>
  executeJsonApiRequest('/ListOrdersPlanner', 'POST', { userId })
