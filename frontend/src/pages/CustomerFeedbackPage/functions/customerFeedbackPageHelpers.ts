import {
  buildAttractionOrderDisplay,
  buildFlightOrderDisplay,
  buildHotelOrderDisplay,
  buildTrainOrderDisplay,
  formatFlightClock,
  hasFlightSnapshot,
  hasTrainSnapshot,
  orderMatchesCategory,
  parseAttractionSnapshot,
} from '@/pages/BookingsPage/functions'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

// 根据订单内容生成客服撤单时展示的标题。
export function buildCancellationOrderTitle(order: OrderResponse) {
  const hotelItem = order.orderLineItems.find(isLikelyHotelOrderLineItem)
  if (hotelItem) {
    const display = buildHotelOrderDisplay(hotelItem)
    return joinTitleParts([
      display.hotelName || '酒店订单',
      display.hotelLocation,
      display.roomTypeName,
      display.checkInDate && display.checkOutDate ? `${display.checkInDate} 至 ${display.checkOutDate}` : display.checkInDate || display.checkOutDate,
      formatOrderPrice(order.totalPrice, order.orderCurrency),
    ])
  }

  const trainItem = order.orderLineItems.find(isLikelyTrainOrderLineItem)
  if (trainItem) {
    const display = buildTrainOrderDisplay(trainItem)
    return joinTitleParts([
      display.trainNumber || '火车订单',
      buildRouteLabel(display.departureStationName || display.departureStationCode, display.arrivalStationName || display.arrivalStationCode),
      display.seatClass ?? undefined,
      formatOrderPrice(display.totalPrice || order.totalPrice, display.currency || order.orderCurrency),
    ])
  }

  const attractionItem = order.orderLineItems.find(isLikelyAttractionOrderLineItem)
  if (attractionItem) {
    const display = buildAttractionOrderDisplay(attractionItem)
    return joinTitleParts([
      display.attractionName || '景点订单',
      display.ticketTypeName,
      display.useDate,
      formatOrderPrice(display.totalPrice || order.totalPrice, display.currency || order.orderCurrency),
    ])
  }

  const flightItem = order.orderLineItems.find(isLikelyFlightOrderLineItem) ?? order.orderLineItems[0]
  if (!flightItem) {
    return joinTitleParts([order.orderType || '订单', formatOrderPrice(order.totalPrice, order.orderCurrency)])
  }

  const display = buildFlightOrderDisplay(flightItem)
  return joinTitleParts([
    display.airlineName || '航班订单',
    display.flightNumber,
    buildRouteLabel(display.departureAirport || display.departureCity, display.arrivalAirport || display.arrivalCity),
    display.departureTime ? formatFlightClock(display.departureTime) : undefined,
    display.cabinClass ?? undefined,
    formatOrderPrice(order.totalPrice, order.orderCurrency),
  ])
}

// 根据订单内容判断它属于哪一类订单。
export function inferOrderCategory(order: OrderResponse): OrderCategory {
  if (orderMatchesCategory(order, 'hotelOrders')) return 'hotelOrders'
  if (orderMatchesCategory(order, 'flightOrders')) return 'flightOrders'
  if (orderMatchesCategory(order, 'trainOrders')) return 'trainOrders'
  return 'attractionOrders'
}

// 判断当前行项目是不是航班订单明细，便于拼接撤单标题。
function isLikelyFlightOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.flightDetails) return true
  return hasFlightSnapshot(summaryLabel) || summaryLabel.toLowerCase().includes('"flightnumber"')
}

// 判断当前行项目是不是酒店订单明细，便于拼接撤单标题。
function isLikelyHotelOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.hotelDetails) return true
  if (!summaryLabel.startsWith('{')) return false
  const normalized = summaryLabel.toLowerCase()
  return normalized.includes('"hotelname"') || normalized.includes('"roomtypename"') || normalized.includes('"checkindate"')
}

// 判断当前行项目是不是火车订单明细，便于拼接撤单标题。
function isLikelyTrainOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.trainDetails) return true
  return hasTrainSnapshot(summaryLabel) || summaryLabel.toLowerCase().includes('"trainnumber"')
}

// 判断当前行项目是不是景点订单明细，便于拼接撤单标题。
function isLikelyAttractionOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.attractionDetails) return true
  return !!parseAttractionSnapshot(summaryLabel)
}

// 拼出订单路线标题，起点或终点缺失时用默认文案兜底。
function buildRouteLabel(departure: string, arrival: string) {
  const left = departure.trim() || '出发地未知'
  const right = arrival.trim() || '到达地未知'
  return `${left} → ${right}`
}

// 把金额和币种拼成可读文案。
function formatOrderPrice(totalPrice: string, orderCurrency: string) {
  const price = totalPrice.trim()
  const currency = orderCurrency.trim()
  if (!price && !currency) return undefined
  return [price, currency].filter(Boolean).join(' ')
}

// 把标题片段按统一规则拼接起来。
function joinTitleParts(parts: Array<string | undefined | null>) {
  return parts
    .map(part => (typeof part === 'string' ? part.trim() : ''))
    .filter(part => part.length > 0)
    .join(' · ')
}
