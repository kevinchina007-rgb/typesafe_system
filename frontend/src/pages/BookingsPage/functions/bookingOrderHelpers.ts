import type { AppLanguage, OrderLineItemResponse, OrderResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { localizePaymentMethod } from '@/lib/presenters/view-models'
import { hasTrainSnapshot, parseAttractionSnapshot, parseFlightSnapshot, parseHotelSnapshot } from './bookingOrderDisplayHelpers'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

// 查找订单项对应的评价记录，只做结果匹配，不做额外过滤。
export function findOrderItemReview(reviews: ReviewResponse[], orderItemId: string) {
  return reviews.find(review => review.orderItemId === orderItemId) ?? null
}

// 把出行人 ID 转成页面上的显示文本。
export function formatTravelerIdentity(travelers: TravelerResponse[], travelerId: string): string {
  const matchedTraveler = travelers.find(traveler => traveler.travelerId === travelerId)
  if (!matchedTraveler) {
    return travelerId
  }

  return `${matchedTraveler.fullName} (${matchedTraveler.documentNumber.slice(-4)})`
}

// 汇总订单支付记录，供订单卡片直接展示。
export function buildOrderPaymentSummary(order: OrderResponse, currentLanguage: AppLanguage) {
  return (order.orderPayments ?? [])
    .map(payment => `${localizePaymentMethod(payment.paymentMethod, currentLanguage)} ${payment.paymentAmount} ${payment.paymentCurrency}`)
    .join(' | ')
}

// 汇总订单退款记录，供订单卡片直接展示。
export function buildOrderRefundSummary(order: OrderResponse, currentLanguage: AppLanguage, mapBackendStatusToProductLabel: (value: string, language: AppLanguage) => string) {
  return (order.orderRefunds ?? [])
    .map(refund => `${refund.refundAmount} ${refund.refundCurrency} ${mapBackendStatusToProductLabel(refund.refundStatus, currentLanguage)}`)
    .join(' | ')
}

// 判断订单行是否携带可展示的详情信息。
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

// 判断订单项是不是航班类订单项。
export function isFlightOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Flight') return true
  if (orderLineItem.orderItemKind === 'Hotel' || orderLineItem.orderItemKind === 'Train' || orderLineItem.orderItemKind === 'Attraction') return false
  return !!orderLineItem.flightDetails || !!parseFlightSnapshot(orderLineItem.summaryLabel)
}

// 判断订单项是不是酒店类订单项。
export function isHotelOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Hotel') return true
  if (orderLineItem.orderItemKind === 'Flight' || orderLineItem.orderItemKind === 'Train' || orderLineItem.orderItemKind === 'Attraction') return false
  return !!orderLineItem.hotelDetails || !!parseHotelSnapshot(orderLineItem.summaryLabel)
}

// 判断订单项是不是火车类订单项。
export function isTrainOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Train') return true
  if (orderLineItem.orderItemKind === 'Flight' || orderLineItem.orderItemKind === 'Hotel' || orderLineItem.orderItemKind === 'Attraction') return false
  return !!orderLineItem.trainDetails || hasTrainSnapshot(orderLineItem.summaryLabel)
}

// 判断订单项是不是景点类订单项。
export function isAttractionOrderLineItem(orderLineItem: OrderLineItemResponse) {
  if (orderLineItem.orderItemKind === 'Attraction') return true
  if (orderLineItem.orderItemKind === 'Flight' || orderLineItem.orderItemKind === 'Hotel' || orderLineItem.orderItemKind === 'Train') return false
  return !!orderLineItem.attractionDetails || !!parseAttractionSnapshot(orderLineItem.summaryLabel)
}

// 判断订单是否属于指定分类，用于订单列表筛选。
export function orderMatchesCategory(order: OrderResponse, orderCategory: OrderCategory) {
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

// 判断订单状态是否允许支付。
export function isOrderPayable(status: string) {
  return status === 'Draft' || status === 'PendingPayment' || status === 'PendingSelection'
}

// 判断订单状态是否已经支付完成。
export function isOrderPaid(status: string) {
  return status === 'Confirmed' || status === 'Paid' || status === 'Booked'
}

// 判断订单状态是否已经退款。
export function isOrderRefunded(status: string) {
  return status === 'Refunded'
}
