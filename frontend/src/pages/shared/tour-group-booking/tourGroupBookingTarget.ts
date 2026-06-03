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

const storageKey = 'flypig.tourGroupBookingTarget'

export type TourGroupBookingTargetForView<K extends TourGroupBookingTarget['viewKey']> = Extract<TourGroupBookingTarget, { viewKey: K }>

export function storeTourGroupBookingTarget(target: TourGroupBookingTarget) {
  window.sessionStorage.setItem(storageKey, JSON.stringify(target))
}

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
