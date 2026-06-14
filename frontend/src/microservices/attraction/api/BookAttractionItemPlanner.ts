import type { BookAttractionItemRequest } from '@/microservices/attraction/objects/BookAttractionItemRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const addAttractionItemToOrder = (orderId: string, payload: BookAttractionItemRequest): Promise<OrderResponse> =>
  executeJsonApiRequest('/BookAttractionItemPlanner', 'POST', { ...payload, orderId })
    .then(() => executeJsonApiRequest('/GetOrderPlanner', 'POST', { orderId }))
