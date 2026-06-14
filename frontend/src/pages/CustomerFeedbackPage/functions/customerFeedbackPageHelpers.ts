import {
  buildAttractionOrderDisplay,
  buildFlightOrderDisplay,
  buildHotelOrderDisplay,
  buildTrainOrderDisplay,
  formatFlightClock,
  hasFlightSnapshot,
  hasTrainSnapshot,
  orderMatchesCategory,
  orderTypeToOrderCategory,
  parseAttractionSnapshot,
} from '@/pages/BookingsPage/functions'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

export function buildCancellationOrderTitle(order: OrderResponse) {
  const orderCategory = orderTypeToOrderCategory(order.orderType)

  if (orderCategory === 'hotelOrders') {
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
  }

  if (orderCategory === 'trainOrders') {
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
  }

  if (orderCategory === 'attractionOrders') {
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
  }

  if (orderCategory === 'flightOrders') {
    const flightItem = order.orderLineItems.find(isLikelyFlightOrderLineItem) ?? order.orderLineItems[0]
    if (flightItem) {
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
  }

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

export function inferOrderCategory(order: OrderResponse): OrderCategory {
  const orderTypeCategory = orderTypeToOrderCategory(order.orderType)
  if (orderTypeCategory) return orderTypeCategory
  if (orderMatchesCategory(order, 'hotelOrders')) return 'hotelOrders'
  if (orderMatchesCategory(order, 'flightOrders')) return 'flightOrders'
  if (orderMatchesCategory(order, 'trainOrders')) return 'trainOrders'
  return 'attractionOrders'
}

function isLikelyFlightOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.flightDetails) return true
  return hasFlightSnapshot(summaryLabel) || summaryLabel.toLowerCase().includes('"flightnumber"')
}

function isLikelyHotelOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.hotelDetails) return true
  if (!summaryLabel.startsWith('{')) return false
  const normalized = summaryLabel.toLowerCase()
  return normalized.includes('"hotelname"') || normalized.includes('"roomtypename"') || normalized.includes('"checkindate"')
}

function isLikelyTrainOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.trainDetails) return true
  return hasTrainSnapshot(summaryLabel) || summaryLabel.toLowerCase().includes('"trainnumber"')
}

function isLikelyAttractionOrderLineItem(orderLineItem: OrderResponse['orderLineItems'][number]) {
  const summaryLabel = orderLineItem.summaryLabel.trim()
  if (orderLineItem.attractionDetails) return true
  return !!parseAttractionSnapshot(summaryLabel)
}

function buildRouteLabel(departure: string, arrival: string) {
  const left = departure.trim() || '出发地未知'
  const right = arrival.trim() || '到达地未知'
  return `${left} → ${right}`
}

function formatOrderPrice(totalPrice: string, orderCurrency: string) {
  const price = totalPrice.trim()
  const currency = orderCurrency.trim()
  if (!price && !currency) return undefined
  return [price, currency].filter(Boolean).join(' ')
}

function joinTitleParts(parts: Array<string | undefined | null>) {
  return parts
    .map(part => (typeof part === 'string' ? part.trim() : ''))
    .filter(part => part.length > 0)
    .join(' · ')
}
