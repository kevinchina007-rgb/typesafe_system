import type { AppLanguage, AppViewKey, OrderLineItemResponse, OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'

export type OrderCategory = Extract<AppViewKey, 'flightOrders' | 'hotelOrders' | 'trainOrders' | 'attractionOrders'>

export type OrderPanelProps = {
  currentLanguage: AppLanguage
  orderCategory: OrderCategory
  isBusy: boolean
  isGuestMode: boolean
  orders: OrderResponse[]
  reviews: ReviewResponse[]
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onReloadOrders: () => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
  onCancelOrder: (orderId: string) => Promise<void>
  onRequestRefund: (orderId: string, refundReason: string) => Promise<void>
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
}

export function findOrderItemReview(reviews: ReviewResponse[], orderItemId: string) {
  return reviews.find(review => review.orderItemId === orderItemId) ?? null
}

export function formatTravelerIdentity(travelers: TravelerResponse[], travelerId: string): string {
  const matchedTraveler = travelers.find(traveler => traveler.travelerId === travelerId)
  if (!matchedTraveler) {
    return travelerId
  }

  return `${matchedTraveler.fullName} (${matchedTraveler.documentNumber.slice(-4)})`
}

export function buildOrderPaymentSummary(order: OrderResponse, localizePaymentMethod: (value: string, language: AppLanguage) => string, currentLanguage: AppLanguage) {
  return (order.orderPayments ?? [])
    .map(payment => `${localizePaymentMethod(payment.paymentMethod, currentLanguage)} ${payment.paymentAmount} ${payment.paymentCurrency}`)
    .join(' | ')
}

export function buildOrderRefundSummary(order: OrderResponse, currentLanguage: AppLanguage, mapBackendStatusToProductLabel: (value: string, language: AppLanguage) => string) {
  return (order.orderRefunds ?? [])
    .map(refund => `${refund.refundAmount} ${refund.refundCurrency} ${mapBackendStatusToProductLabel(refund.refundStatus, currentLanguage)}`)
    .join(' | ')
}

export function hasOrderLineItemDetails(orderLineItem: OrderLineItemResponse) {
  return !!(
    orderLineItem.flightDetails ||
    orderLineItem.hotelDetails ||
    orderLineItem.trainDetails ||
    orderLineItem.attractionDetails
  )
}

export function orderMatchesCategory(order: OrderResponse, orderCategory: OrderCategory) {
  const normalizedOrderType = order.orderType.toLowerCase()
  const orderLineItems = order.orderLineItems ?? []
  if (orderCategory === 'flightOrders') {
    return normalizedOrderType.includes('flight') || orderLineItems.some(item => item.flightDetails)
  }
  if (orderCategory === 'hotelOrders') {
    return normalizedOrderType.includes('hotel') || orderLineItems.some(item => item.hotelDetails)
  }
  if (orderCategory === 'trainOrders') {
    return normalizedOrderType.includes('train') || orderLineItems.some(item => item.trainDetails)
  }
  return normalizedOrderType.includes('attraction') || orderLineItems.some(item => item.attractionDetails)
}
