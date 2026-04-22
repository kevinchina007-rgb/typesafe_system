import type {
  AppLanguage,
  OrderLineItemResponse,
  OrderResponse,
  ReviewResponse,
  TravelerResponse,
} from '../../lib/mvp-types'

export type OrderPanelProps = {
  currentLanguage: AppLanguage
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
  onOpenFeedbackForReview: (reviewId: string) => Promise<void>
  onStartReviewInFeedback: (payload: PendingReviewTarget) => Promise<void>
}

export type PendingReviewTarget = {
  orderId: string
  orderItemId: string
  title: string
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
