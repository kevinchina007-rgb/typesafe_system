import type { BookHotelPlannerRequest } from '@/microservices/hotel/objects/BookHotelPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createHotelOrder = async (payload: BookHotelPlannerRequest): Promise<OrderResponse> => {
  const response = await executeJsonApiRequest<{ orderId: string }>('/BookHotelPlanner', 'POST', payload)
  return executeJsonApiRequest('/GetOrderPlanner', 'POST', { orderId: response.orderId })
}
