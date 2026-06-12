// 本文件定义团组预订目标序列化与读取逻辑，负责保存当前预订对象。

// 团组预订目标的统一数据结构。
export type TourGroupBookingTarget =
  | {
      viewKey: 'flights'
      flightId: string
      cabinClass: string | null
      departureAirport: string
      arrivalAirport: string
      departureDate: string
    }
  | {
      viewKey: 'hotels'
      roomTypeId: string
      hotelId: string
      checkInDate: string
      checkOutDate: string
    }
  | {
      viewKey: 'trains'
      trainId: string
      seatClass: string | null
      fromStationCode: string
      toStationCode: string
      date: string
    }
  | {
      viewKey: 'attractions'
      attractionId: string
      ticketTypeId: string
      sessionId: string | null
      useDate: string
    }

// sessionStorage 里保存团组预订目标时使用的固定 key。
const storageKey = 'flypig.tourGroupBookingTarget'

// 按具体视图类型收窄后的团组预订目标。
export type TourGroupBookingTargetForView<K extends TourGroupBookingTarget['viewKey']> = Extract<TourGroupBookingTarget, { viewKey: K }>

// 把当前团组预订目标写入会话缓存。
export function storeTourGroupBookingTarget(target: TourGroupBookingTarget) {
  window.sessionStorage.setItem(storageKey, JSON.stringify(target))
}

// 从会话缓存中读取当前团组预订目标。
export function readTourGroupBookingTarget(): TourGroupBookingTarget | null {
  const raw = window.sessionStorage.getItem(storageKey)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as TourGroupBookingTarget
  } catch {
    return null
  }
}

// 读取并消费当前团组预订目标，使用后立即清空缓存。
export function consumeTourGroupBookingTarget<K extends TourGroupBookingTarget['viewKey']>(
  expectedViewKey: K,
): TourGroupBookingTargetForView<K> | null
export function consumeTourGroupBookingTarget(expectedViewKey?: undefined): TourGroupBookingTarget | null
export function consumeTourGroupBookingTarget(expectedViewKey?: TourGroupBookingTarget['viewKey']) {
  const target = readTourGroupBookingTarget()
  if (!target) {
    return null
  }

  if (expectedViewKey && target.viewKey !== expectedViewKey) {
    return null
  }

  window.sessionStorage.removeItem(storageKey)
  return target
}
