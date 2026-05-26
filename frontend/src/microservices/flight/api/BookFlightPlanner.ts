import type { BookFlightRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createFlightOrder = (payload: BookFlightRequest): Promise<OrderResponse> =>
  executeJsonApiRequest('/BookFlightPlanner', 'POST', payload)
