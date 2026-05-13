import type { OrderListResponse } from '@/microservices/order/objects/OrderListResponse'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'

import type { BookAttractionItemRequest } from '@/microservices/attraction/objects/BookAttractionItemRequest'
import type { BookHotelRequest } from '@/microservices/hotel/objects/BookHotelRequest'
import type { BookTrainItemRequest } from '@/microservices/train/objects/BookTrainItemRequest'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createOrder = (payload: { ownerUserId: string; orderCurrency: string }): Promise<OrderResponse> =>
    executeJsonApiRequest('/orders', 'POST', payload)

export const addTrainItemToOrder = (orderId: string, payload: BookTrainItemRequest): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/train-items`, 'POST', payload)

export const addAttractionItemToOrder = (orderId: string, payload: BookAttractionItemRequest): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/attraction-items`, 'POST', payload)

export const createHotelOrder = (payload: BookHotelRequest): Promise<OrderResponse> =>
    executeJsonApiRequest('/hotels/book', 'POST', payload)

export const getOrder = (orderId: string): Promise<OrderResponse> =>
    executeApiRequest(`/orders/${orderId}`)

export const listOrders = (userId: string): Promise<OrderListResponse> =>
    executeApiRequest(`/users/${userId}/orders`)

export const payOrder = (orderId: string, payload: { paymentMethod: string; paymentSucceeded: boolean }): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/pay`, 'POST', payload)

export const cancelOrder = (orderId: string): Promise<OrderResponse> =>
    executeApiRequest(`/orders/${orderId}/cancel`, { method: 'POST' })

export const requestRefund = (orderId: string, payload: { refundReason: string }): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/refunds`, 'POST', payload)

export const approveRefund = (orderId: string, managerId: string, managerType: string): Promise<OrderResponse> =>
    executeApiRequest(`/manager/orders/${orderId}/refund/approve${createQueryString({ managerId, managerType })}`, { method: 'POST' })

export const rejectRefund = (orderId: string, managerId: string, managerType: string): Promise<OrderResponse> =>
    executeApiRequest(`/manager/orders/${orderId}/refund/reject${createQueryString({ managerId, managerType })}`, { method: 'POST' })
