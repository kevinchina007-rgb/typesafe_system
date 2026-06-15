import type { BookAttractionItemPlannerRequest } from '@/microservices/attraction/objects/BookAttractionItemPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const addAttractionItemToOrder = (orderId: string, payload: BookAttractionItemPlannerRequest): Promise<OrderResponse> =>
  executeJsonApiRequest('/BookAttractionItemPlanner', 'POST', { ...payload, orderId })
    .then(() => executeJsonApiRequest('/GetOrderPlanner', 'POST', { orderId }))
