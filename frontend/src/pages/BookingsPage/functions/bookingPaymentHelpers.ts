import type { OrderResponse } from '@/lib/mvp-types/index'

// 支付方式的页面级枚举值，只用于前端展示和请求转发。
export type PaymentMethodValue = 'alipay' | 'wechat-pay' | 'nailong-pay'

// 把支付方式转换成页面上显示的中文名称。
export function getPaymentMethodLabel(paymentMethod: PaymentMethodValue) {
  switch (paymentMethod) {
    case 'alipay':
      return '支付宝'
    case 'wechat-pay':
      return '微信支付'
    case 'nailong-pay':
      return '奶龙支付'
  }
}

// 支付方式选项列表，供支付弹窗直接渲染。
export const paymentMethodOptions: Array<{ value: PaymentMethodValue; label: string }> = [
  { value: 'alipay', label: '支付宝' },
  { value: 'wechat-pay', label: '微信支付' },
  { value: 'nailong-pay', label: '奶龙支付' },
]

// 判断一个订单是不是航班订单，供支付和订单展示逻辑复用。
export function isFlightOrder(order: OrderResponse) {
  if (!order.orderType.toLowerCase().includes('flight')) {
    return false
  }
  return (order.orderLineItems ?? []).some(orderLineItem => orderLineItem.flightDetails || hasFlightSnapshot(orderLineItem.summaryLabel))
}

// 读取航班订单里关联的出行人 ID。
export function getFlightDetailsPlannerOrderTravelerIds(order: OrderResponse) {
  if (!isFlightOrder(order)) {
    return []
  }
  const flightItem = (order.orderLineItems ?? []).find(orderLineItem => orderLineItem.flightDetails || hasFlightSnapshot(orderLineItem.summaryLabel))
  if (!flightItem) {
    return []
  }
  return flightItem.flightDetails?.travelerIds ?? parseFlightSnapshot(flightItem.summaryLabel)?.travelerIds ?? []
}

// 判断摘要文本里是否携带航班快照。
export function hasFlightSnapshot(summaryLabel: string) {
  const snapshot = parseFlightSnapshot(summaryLabel)
  return !!snapshot && (
    !!snapshot.airlineName ||
    !!snapshot.flightNumber ||
    !!snapshot.flightId ||
    !!snapshot.departureAirport ||
    !!snapshot.arrivalAirport ||
    !!snapshot.departureAirportCode ||
    !!snapshot.arrivalAirportCode ||
    !!snapshot.cabinClass
  )
}

// 解析航班快照文本，只做数据读取，不做业务判断。
function parseFlightSnapshot(summaryLabel: string): {
  airlineName?: string
  flightNumber?: string
  flightId?: string
  departureAirport?: string
  arrivalAirport?: string
  departureAirportCode?: string
  arrivalAirportCode?: string
  cabinClass?: string
  travelerIds: string[]
} | null {
  if (!summaryLabel.trim().startsWith('{')) {
    return null
  }

  try {
    const parsed = JSON.parse(summaryLabel) as {
      airlineName?: unknown
      flightNumber?: unknown
      flightId?: unknown
      departureAirport?: unknown
      arrivalAirport?: unknown
      departureAirportCode?: unknown
      arrivalAirportCode?: unknown
      cabinClass?: unknown
      travelerIds?: unknown
    }
    return {
      airlineName: typeof parsed.airlineName === 'string' ? parsed.airlineName : undefined,
      flightNumber: typeof parsed.flightNumber === 'string' ? parsed.flightNumber : undefined,
      flightId: typeof parsed.flightId === 'string' ? parsed.flightId : undefined,
      departureAirport: typeof parsed.departureAirport === 'string' ? parsed.departureAirport : undefined,
      arrivalAirport: typeof parsed.arrivalAirport === 'string' ? parsed.arrivalAirport : undefined,
      departureAirportCode: typeof parsed.departureAirportCode === 'string' ? parsed.departureAirportCode : undefined,
      arrivalAirportCode: typeof parsed.arrivalAirportCode === 'string' ? parsed.arrivalAirportCode : undefined,
      cabinClass: typeof parsed.cabinClass === 'string' ? parsed.cabinClass : undefined,
      travelerIds: Array.isArray(parsed.travelerIds) ? parsed.travelerIds.filter((value): value is string => typeof value === 'string' && value.trim().length > 0) : [],
    }
  } catch {
    return null
  }
}
