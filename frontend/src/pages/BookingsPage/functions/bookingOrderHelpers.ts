import type { AppLanguage, OrderLineItemResponse, OrderResponse, ReviewPlannerResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { localizePaymentMethod } from '@/lib/presenters/view-models'
import { hasTrainSnapshot, parseAttractionSnapshot, parseFlightSnapshot, parseHotelSnapshot } from './bookingOrderDisplayHelpers'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

export function findOrderItemReview(reviews: ReviewPlannerResponse[], orderItemId: string) {
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
    parseAttractionSnapshot(orderLineItem.summaryLabel) ||
    hasTrainSnapshot(orderLineItem.summaryLabel)
  )
}

export function isFlightOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Flight') return true
  if (orderLineItem.orderItemKind === 'Hotel' || orderLineItem.orderItemKind === 'Train' || orderLineItem.orderItemKind === 'Attraction') return false
  return !!orderLineItem.flightDetails || !!parseFlightSnapshot(orderLineItem.summaryLabel)
}

export function isHotelOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Hotel') return true
  if (orderLineItem.orderItemKind === 'Flight' || orderLineItem.orderItemKind === 'Train' || orderLineItem.orderItemKind === 'Attraction') return false
  return !!orderLineItem.hotelDetails || !!parseHotelSnapshot(orderLineItem.summaryLabel)
}

export function isTrainOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Train') return true
  if (orderLineItem.orderItemKind === 'Flight' || orderLineItem.orderItemKind === 'Hotel' || orderLineItem.orderItemKind === 'Attraction') return false
  return !!orderLineItem.trainDetails || hasTrainSnapshot(orderLineItem.summaryLabel)
}

export function isAttractionOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Attraction') return true
  if (orderLineItem.orderItemKind === 'Flight' || orderLineItem.orderItemKind === 'Hotel' || orderLineItem.orderItemKind === 'Train') return false
  return !!orderLineItem.attractionDetails || !!parseAttractionSnapshot(orderLineItem.summaryLabel)
}

export function orderTypeToOrderCategory(orderType: string): OrderCategory | null {
  const normalizedOrderType = orderType.trim().toLowerCase()
  if (normalizedOrderType.includes('flight')) return 'flightOrders'
  if (normalizedOrderType.includes('hotel')) return 'hotelOrders'
  if (normalizedOrderType.includes('train')) return 'trainOrders'
  if (normalizedOrderType.includes('attraction')) return 'attractionOrders'
  return null
}

export function orderMatchesCategory(order: OrderResponse, orderCategory: OrderCategory) {
  const orderTypeCategory = orderTypeToOrderCategory(order.orderType)
  if (orderTypeCategory) {
    return orderTypeCategory === orderCategory
  }

  const orderLineItems = order.orderLineItems ?? []
  if (orderCategory === 'flightOrders') {
    const flightLineItems = orderLineItems.filter(item => isFlightOrderLineItem(item))
    return flightLineItems.length > 0 && flightLineItems.length === orderLineItems.length
  }
  if (orderCategory === 'hotelOrders') {
    const hotelLineItems = orderLineItems.filter(item => isHotelOrderLineItem(item))
    return hotelLineItems.length > 0 && hotelLineItems.length === orderLineItems.length
  }
  if (orderCategory === 'trainOrders') {
    const trainLineItems = orderLineItems.filter(item => isTrainOrderLineItem(item))
    return trainLineItems.length > 0 && trainLineItems.length === orderLineItems.length
  }
  const attractionLineItems = orderLineItems.filter(item => isAttractionOrderLineItem(item))
  return attractionLineItems.length > 0 && attractionLineItems.length === orderLineItems.length
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
