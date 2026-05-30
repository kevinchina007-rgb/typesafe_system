import type { AppLanguage, OrderLineItemResponse, OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { localizePaymentMethod } from '@/lib/presenters/view-models'
import { hasTrainSnapshot } from './bookingOrderDisplayHelpers'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

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

export function buildOrderPaymentSummary(order: OrderResponse, currentLanguage: AppLanguage) {
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
    orderLineItem.attractionDetails ||
    hasTrainSnapshot(orderLineItem.summaryLabel)
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
    const trainLineItems = orderLineItems.filter(item => item.trainDetails || hasTrainSnapshot(item.summaryLabel))
    return trainLineItems.length > 0 && trainLineItems.length === orderLineItems.length
  }
  return normalizedOrderType.includes('attraction') || orderLineItems.some(item => item.attractionDetails)
}

export function isOrderPayable(status: string) {
  return status === 'Draft' || status === 'PendingPayment' || status === 'PendingSelection'
}

export function isOrderPaid(status: string) {
  return status === 'Confirmed' || status === 'Paid' || status === 'Booked'
}

export function isOrderRefunded(status: string) {
  return status === 'Refunded'
}
