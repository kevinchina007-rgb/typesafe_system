import type { OrderListResponse } from '@/microservices/order/objects/OrderListResponse'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'

import type { BookAttractionItemRequest } from '@/microservices/attraction/objects/BookAttractionItemRequest'
import type { BookHotelPlannerRequest } from '@/microservices/hotel/objects/BookHotelPlannerRequest'
import type { BookTrainItemRequest } from '@/microservices/train/objects/BookTrainItemRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createOrder = (payload: { ownerUserId: string; orderCurrency: string }): Promise<OrderResponse> =>
    executeJsonApiRequest('/CreateOrderPlanner', 'POST', payload)

export const addTrainItemToOrder = async (orderId: string, payload: BookTrainItemRequest): Promise<OrderResponse> => {
    await executeJsonApiRequest('/BookTrainItemPlanner', 'POST', { ...payload, orderId })
    return getOrder(orderId)
}

export const addAttractionItemToOrder = (orderId: string, payload: BookAttractionItemRequest): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/attraction-items`, 'POST', payload)

export const createHotelOrder = async (payload: BookHotelPlannerRequest): Promise<OrderResponse> => {
    const response = await executeJsonApiRequest<{ orderId: string }>('/BookHotelPlanner', 'POST', payload)
    return getOrder(response.orderId)
}

export const getOrder = (orderId: string): Promise<OrderResponse> =>
    executeJsonApiRequest('/GetOrderPlanner', 'POST', { orderId })

export const listOrders = (userId: string): Promise<OrderListResponse> =>
    executeJsonApiRequest('/ListOrdersPlanner', 'POST', { userId })

export const payOrder = (orderId: string, payload: { paymentMethod: string; paymentSucceeded: boolean; travelerIds?: string[] }): Promise<OrderResponse> =>
    executeJsonApiRequest('/PayOrderPlanner', 'POST', { orderId, ...payload })

export const cancelOrder = (orderId: string): Promise<OrderResponse> =>
    executeJsonApiRequest('/CancelOrderPlanner', 'POST', { orderId })

export const requestRefund = (orderId: string, payload: { refundReason: string }): Promise<OrderResponse> =>
    executeJsonApiRequest('/RequestRefundPlanner', 'POST', { orderId, ...payload })

export const approveRefund = (_orderId: string, managerId: string, managerType: string): Promise<unknown> =>
    executeJsonApiRequest('/ApproveManagerRefundPlanner', 'POST', { managerId, managerType })

export const rejectRefund = (_orderId: string, managerId: string, managerType: string): Promise<unknown> =>
    executeJsonApiRequest('/RejectManagerRefundPlanner', 'POST', { managerId, managerType })
