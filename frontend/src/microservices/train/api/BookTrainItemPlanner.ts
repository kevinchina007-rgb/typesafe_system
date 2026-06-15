import type { BookTrainItemPlannerRequest } from '@/microservices/train/objects/BookTrainItemPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const bookTrainItemPlanner = async (orderId: string, payload: BookTrainItemPlannerRequest): Promise<OrderResponse> => {
  await executeJsonApiRequest('/BookTrainItemPlanner', 'POST', { ...payload, orderId })
  return executeJsonApiRequest('/GetOrderPlanner', 'POST', { orderId })
}

export const addTrainItemToOrder = bookTrainItemPlanner
