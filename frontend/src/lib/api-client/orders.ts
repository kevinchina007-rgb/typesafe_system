import type { OrderListResponse, OrderResponse, PaymentLinkResponse } from '../api-dtos'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const orderApiClient = {
  createOrder: (payload: { ownerUserId: string; orderCurrency: string }): Promise<OrderResponse> =>
    executeJsonApiRequest('/orders', 'POST', payload),

  addTrainItemToOrder: (
    orderId: string,
    payload: {
      buyerUserId: string
      orderId: string
      trainId: string
      travelerIds: string[]
      fromStationCode: string
      toStationCode: string
      seatClass: string
      seatPreference?: string | null
    },
  ): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/train-items`, 'POST', payload),

  addAttractionItemToOrder: (
    orderId: string,
    payload: {
      buyerUserId: string
      orderId: string
      attractionId: string
      ticketTypeId: string
      sessionId?: string | null
      travelerIds: string[]
      useDate: string
    },
  ): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/attraction-items`, 'POST', payload),

  createFlightOrder: (payload: { buyerUserId: string; flightId: string; travelerIds: string[]; cabinClass: string }): Promise<OrderResponse> =>
    executeJsonApiRequest('/flights/book', 'POST', payload),

  createHotelOrder: (
    payload: {
      buyerUserId: string
      roomTypeId: string
      guestTravelerIds: string[]
      checkInDate: string
      checkOutDate: string
      roomCount: number
    },
  ): Promise<OrderResponse> =>
    executeJsonApiRequest('/hotels/book', 'POST', payload),

  getOrder: (orderId: string): Promise<OrderResponse> =>
    executeApiRequest(`/orders/${orderId}`),

  listOrders: (userId: string): Promise<OrderListResponse> =>
    executeApiRequest(`/users/${userId}/orders`),

  payOrder: (orderId: string, payload: { paymentMethod: string; paymentSucceeded: boolean }): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/pay`, 'POST', payload),

  createPaymentLink: (orderId: string, paymentMethod: string, language: 'en' | 'zh'): Promise<PaymentLinkResponse> =>
    executeApiRequest(`/orders/${orderId}/payment-link${createQueryString({ paymentMethod, lang: language })}`),

  cancelOrder: (orderId: string): Promise<OrderResponse> =>
    executeApiRequest(`/orders/${orderId}/cancel`, { method: 'POST' }),

  requestRefund: (orderId: string, payload: { refundReason: string }): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/refunds`, 'POST', payload),

  approveRefund: (orderId: string, managerId: string, managerType: string): Promise<OrderResponse> =>
    executeApiRequest(`/manager/orders/${orderId}/refund/approve${createQueryString({ managerId, managerType })}`, { method: 'POST' }),

  rejectRefund: (orderId: string, managerId: string, managerType: string): Promise<OrderResponse> =>
    executeApiRequest(`/manager/orders/${orderId}/refund/reject${createQueryString({ managerId, managerType })}`, { method: 'POST' }),
}
