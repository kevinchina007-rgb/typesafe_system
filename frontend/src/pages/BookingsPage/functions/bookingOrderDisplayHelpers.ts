import type { AppLanguage, OrderLineItemResponse } from '@/lib/mvp-types/index'
import { localizeBookingKind, localizeTrainSeatClass } from '@/lib/presenters/view-models'
import { formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import {
  getFlightDetailsPlannerAirlineDisplayNameByCode,
  getFlightDetailsPlannerAirlineLogoPathByCode,
} from '@/app/stores/models/flights/flightAirlineCatalog'
import type {
  FlightOrderDisplay,
  FlightSnapshotSummary,
  HotelOrderDisplay,
  HotelSnapshotSummary,
  TrainOrderDisplay,
  TrainSnapshotSummary,
} from '@/pages/BookingsPage/objects'

export function parseFlightSnapshot(summaryLabel: string): FlightSnapshotSummary | null {
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
      travelerIds: Array.isArray(parsed.travelerIds) ? parsed.travelerIds.filter((value): value is string => typeof value === 'string') : [],
    }
  } catch {
    return null
  }
}

export function parseHotelSnapshot(summaryLabel: string): HotelSnapshotSummary | null {
  if (!summaryLabel.trim().startsWith('{')) {
    return null
  }

  try {
    const parsed = JSON.parse(summaryLabel) as Record<string, unknown>
    return {
      hotelName: getStringField(parsed, 'hotelName'),
      hotelLocation: getStringField(parsed, 'hotelLocation'),
      roomTypeName: getStringField(parsed, 'roomTypeName'),
      roomCount: getNumberField(parsed, 'roomCount'),
      checkInDate: getStringField(parsed, 'checkInDate'),
      checkOutDate: getStringField(parsed, 'checkOutDate'),
      travelerIds: getStringArrayField(parsed, 'travelerIds'),
    }
  } catch {
    return null
  }
}

export function parseTrainSnapshot(summaryLabel: string): TrainSnapshotSummary | null {
  if (!summaryLabel.trim().startsWith('{')) {
    return null
  }

  try {
    const parsed = JSON.parse(summaryLabel) as {
      trainId?: unknown
      trainNumber?: unknown
      seatClass?: unknown
      departureStation?: unknown
      departureStationCode?: unknown
      arrivalStation?: unknown
      arrivalStationCode?: unknown
      departureTime?: unknown
      arrivalTime?: unknown
      requestedSeatPreference?: unknown
      seatAssignments?: unknown
      travelerIds?: unknown
      unitPrice?: unknown
      totalPrice?: unknown
      currency?: unknown
    }
    const seatAssignments = Array.isArray(parsed.seatAssignments)
      ? parsed.seatAssignments
          .map(seatAssignment => {
            if (seatAssignment && typeof seatAssignment === 'object') {
              const typedSeatAssignment = seatAssignment as Record<string, unknown>
              return {
                travelerId: typeof typedSeatAssignment.travelerId === 'string' ? typedSeatAssignment.travelerId : undefined,
                carriageNo: typeof typedSeatAssignment.carriageNo === 'number' ? typedSeatAssignment.carriageNo : undefined,
                seatNo: typeof typedSeatAssignment.seatNo === 'string' ? typedSeatAssignment.seatNo : undefined,
                seatLabel: typeof typedSeatAssignment.seatLabel === 'string' ? typedSeatAssignment.seatLabel : undefined,
              }
            }
            return null
          })
          .filter(
            (seatAssignment): seatAssignment is { travelerId: string; carriageNo: number; seatNo: string; seatLabel: string } =>
              !!seatAssignment &&
              typeof seatAssignment.travelerId === 'string' &&
              typeof seatAssignment.carriageNo === 'number' &&
              typeof seatAssignment.seatNo === 'string' &&
              typeof seatAssignment.seatLabel === 'string',
          )
      : []
    const snapshot = {
      trainId: typeof parsed.trainId === 'string' ? parsed.trainId : undefined,
      trainNumber: typeof parsed.trainNumber === 'string' ? parsed.trainNumber : undefined,
      seatClass: typeof parsed.seatClass === 'string' ? parsed.seatClass : undefined,
      departureStation: typeof parsed.departureStation === 'string' ? parsed.departureStation : undefined,
      departureStationCode: typeof parsed.departureStationCode === 'string' ? parsed.departureStationCode : undefined,
      arrivalStation: typeof parsed.arrivalStation === 'string' ? parsed.arrivalStation : undefined,
      arrivalStationCode: typeof parsed.arrivalStationCode === 'string' ? parsed.arrivalStationCode : undefined,
      departureTime: typeof parsed.departureTime === 'string' ? parsed.departureTime : undefined,
      arrivalTime: typeof parsed.arrivalTime === 'string' ? parsed.arrivalTime : undefined,
      requestedSeatPreference: typeof parsed.requestedSeatPreference === 'string' ? parsed.requestedSeatPreference : undefined,
      seatAssignments,
      travelerIds: Array.isArray(parsed.travelerIds) ? parsed.travelerIds.filter((value): value is string => typeof value === 'string') : [],
      unitPrice: typeof parsed.unitPrice === 'string' ? parsed.unitPrice : undefined,
      totalPrice: typeof parsed.totalPrice === 'string' ? parsed.totalPrice : undefined,
      currency: typeof parsed.currency === 'string' ? parsed.currency : undefined,
    }
    return snapshot.trainId ||
      snapshot.trainNumber ||
      snapshot.seatClass ||
      snapshot.departureStation ||
      snapshot.departureStationCode ||
      snapshot.arrivalStation ||
      snapshot.arrivalStationCode ||
      snapshot.departureTime ||
      snapshot.arrivalTime ||
      snapshot.requestedSeatPreference ||
      (snapshot.seatAssignments?.length ?? 0) > 0 ||
      snapshot.travelerIds.length > 0 ||
      snapshot.unitPrice ||
      snapshot.totalPrice ||
      snapshot.currency
      ? snapshot
      : null
  } catch {
    return null
  }
}

export function hasTrainSnapshot(summaryLabel: string) {
  const snapshot = parseTrainSnapshot(summaryLabel)
  return !!snapshot && (
    !!snapshot.trainId ||
    !!snapshot.trainNumber ||
    !!snapshot.seatClass ||
    !!snapshot.departureStation ||
    !!snapshot.departureStationCode ||
    !!snapshot.arrivalStation ||
    !!snapshot.arrivalStationCode ||
    !!snapshot.departureTime ||
    !!snapshot.arrivalTime ||
    !!snapshot.requestedSeatPreference ||
    (snapshot.seatAssignments?.length ?? 0) > 0 ||
    snapshot.travelerIds.length > 0 ||
    !!snapshot.unitPrice ||
    !!snapshot.totalPrice ||
    !!snapshot.currency
  )
}

export function buildFlightOrderDisplay(orderLineItem: OrderLineItemResponse): FlightOrderDisplay {
  const snapshot = parseFlightSnapshot(orderLineItem.summaryLabel)
  const flightDetails = orderLineItem.flightDetails
  const departureAirport = flightDetails?.departureAirport ?? snapshot?.departureAirport ?? snapshot?.departureAirportCode ?? ''
  const arrivalAirport = flightDetails?.arrivalAirport ?? snapshot?.arrivalAirport ?? snapshot?.arrivalAirportCode ?? ''
  return {
    airlineName: flightDetails?.airlineName ?? getFlightDetailsPlannerAirlineDisplayNameByCode(flightDetails?.airlineCode, snapshot?.airlineName ?? ''),
    airlineLogoPath: getFlightDetailsPlannerAirlineLogoPathByCode(flightDetails?.airlineCode, null),
    flightNumber: flightDetails?.flightNumber ?? snapshot?.flightNumber ?? '',
    departureAirport,
    departureCity: formatFlightRouteCity(departureAirport),
    departureTime: flightDetails?.departureTime ?? '',
    arrivalAirport,
    arrivalCity: formatFlightRouteCity(arrivalAirport),
    arrivalTime: flightDetails?.arrivalTime ?? '',
    cabinClass: flightDetails?.cabinClass ?? snapshot?.cabinClass ?? null,
    travelerIds: flightDetails?.travelerIds ?? snapshot?.travelerIds ?? [],
  }
}

export function buildHotelOrderDisplay(orderLineItem: OrderLineItemResponse): HotelOrderDisplay {
  const snapshot = parseHotelSnapshot(orderLineItem.summaryLabel)
  const hotelDetails = orderLineItem.hotelDetails
  return {
    hotelName: hotelDetails?.hotelName ?? snapshot?.hotelName ?? '',
    hotelLocation: hotelDetails?.location ?? snapshot?.hotelLocation ?? '',
    roomTypeName: hotelDetails?.roomTypeName ?? snapshot?.roomTypeName ?? '',
    roomCount: hotelDetails?.roomCount ?? snapshot?.roomCount ?? 0,
    checkInDate: hotelDetails?.checkInDate ?? snapshot?.checkInDate ?? '',
    checkOutDate: hotelDetails?.checkOutDate ?? snapshot?.checkOutDate ?? '',
    guestTravelerIds: hotelDetails?.guestTravelerIds ?? snapshot?.travelerIds ?? [],
  }
}

export function buildTrainOrderDisplay(orderLineItem: OrderLineItemResponse): TrainOrderDisplay {
  const snapshot = parseTrainSnapshot(orderLineItem.summaryLabel)
  const trainDetails = orderLineItem.trainDetails
  const travelerIds = trainDetails?.travelerIds ?? snapshot?.travelerIds ?? []
  const unitPrice = trainDetails?.unitPrice ?? snapshot?.unitPrice ?? null
  const currency = trainDetails?.currency ?? snapshot?.currency ?? orderLineItem.bookedCurrency
  const summaryTotalPrice = snapshot?.totalPrice ?? ''
  const computedTotalPrice = unitPrice ? String(Number(unitPrice) * Math.max(travelerIds.length, 1)) : ''
  const totalPrice = trainDetails?.totalPrice ?? (summaryTotalPrice.trim().length > 0 ? summaryTotalPrice : computedTotalPrice || orderLineItem.bookedAmount)
  return {
    trainId: trainDetails?.trainId ?? snapshot?.trainId ?? '',
    trainNumber: trainDetails?.trainNumber ?? snapshot?.trainNumber ?? '',
    departureStationName: trainDetails?.fromStationName ?? snapshot?.departureStation ?? '',
    departureStationCode: trainDetails?.fromStationCode ?? snapshot?.departureStationCode ?? '',
    arrivalStationName: trainDetails?.toStationName ?? snapshot?.arrivalStation ?? '',
    arrivalStationCode: trainDetails?.toStationCode ?? snapshot?.arrivalStationCode ?? '',
    departureTime: trainDetails?.departureTime ?? snapshot?.departureTime ?? '',
    arrivalTime: trainDetails?.arrivalTime ?? snapshot?.arrivalTime ?? '',
    seatClass: trainDetails?.seatClass ?? snapshot?.seatClass ?? null,
    requestedSeatPreference: trainDetails?.requestedSeatPreference ?? snapshot?.requestedSeatPreference ?? null,
    travelerIds,
    totalPrice,
    currency,
    unitPrice,
    seatAssignments: (trainDetails?.seatAssignments ?? snapshot?.seatAssignments ?? []).map(seatAssignment => ({
      ...seatAssignment,
      seatLabel: formatTrainSeatLabel(seatAssignment.carriageNo, seatAssignment.seatNo, seatAssignment.seatLabel),
    })),
    reservationStatus: trainDetails?.reservationStatus ?? null,
    reservationExpiresAt: trainDetails?.reservationExpiresAt ?? null,
  }
}

export function formatOrderLineItemTitle(orderLineItem: OrderLineItemResponse, snapshotDetails: FlightSnapshotSummary | null, currentLanguage: AppLanguage) {
  if (snapshotDetails) {
    if (snapshotDetails.airlineName || snapshotDetails.flightNumber) {
      return [snapshotDetails.airlineName, snapshotDetails.flightNumber].filter(Boolean).join(' ')
    }
    return localizeBookingKind(orderLineItem.orderItemKind, currentLanguage)
  }

  const trainSnapshot = parseTrainSnapshot(orderLineItem.summaryLabel)
  if (trainSnapshot) {
    const titleParts = [
      trainSnapshot.trainNumber,
      trainSnapshot.departureStation && trainSnapshot.arrivalStation ? `${trainSnapshot.departureStation} → ${trainSnapshot.arrivalStation}` : undefined,
      trainSnapshot.seatClass ? localizeTrainSeatClass(trainSnapshot.seatClass, currentLanguage) : undefined,
    ]
    return titleParts.filter((part): part is string => typeof part === 'string' && part.trim().length > 0).join(' ')
  }

  return orderLineItem.summaryLabel
}

export function formatFlightClock(value: string) {
  if (!value) {
    return ''
  }
  return value.replace('T', ' ').slice(0, 16)
}

export function formatFlightDateTimeRange(departureTime: string, arrivalTime: string) {
  return `${formatFlightClock(departureTime)} → ${formatFlightClock(arrivalTime)}`
}

export function formatTrainDateTimeRange(departureTime: string, arrivalTime: string) {
  return `${formatFlightClock(departureTime)} → ${formatFlightClock(arrivalTime)}`
}

export function formatFlightDate(value: string) {
  if (!value) {
    return ''
  }
  const [datePart] = value.split('T')
  return datePart ?? value
}

export function formatTrainSeatLabel(carriageNo: number, seatNo: string, seatLabel?: string | null) {
  const normalizedSeatLabel = seatLabel?.trim() ?? ''
  if (/^\d{2}\u8f66\s+/.test(normalizedSeatLabel)) {
    return normalizedSeatLabel
  }
  if (normalizedSeatLabel.length > 0 && normalizedSeatLabel.includes('\u8f66') && normalizedSeatLabel.includes(seatNo)) {
    return normalizedSeatLabel
  }
  const normalizedCarriageNo = Number.isFinite(carriageNo) ? String(carriageNo).padStart(2, '0') : '--'
  const normalizedSeatNo = seatNo.trim()
  return normalizedCarriageNo && normalizedSeatNo ? `${normalizedCarriageNo}\u8f66 ${normalizedSeatNo}` : normalizedSeatLabel || normalizedSeatNo || '--'
}

export function getOrderCategoryTitle(orderCategory: string) {
  if (orderCategory === 'flightOrders') {
    return '航班订单'
  }
  if (orderCategory === 'hotelOrders') {
    return '酒店订单'
  }
  if (orderCategory === 'trainOrders') {
    return '火车订单'
  }
  return '景点订单'
}

export function getOrderCategoryDescriptionKey(orderCategory: string) {
  if (orderCategory === 'flightOrders') {
    return 'bookings.flightDescription'
  }
  if (orderCategory === 'hotelOrders') {
    return 'bookings.hotelDescription'
  }
  if (orderCategory === 'trainOrders') {
    return 'bookings.trainDescription'
  }
  return 'bookings.attractionDescription'
}

function getStringField(record: Record<string, unknown>, key: string) {
  const value = record[key]
  return typeof value === 'string' ? value : undefined
}

function getStringArrayField(record: Record<string, unknown>, key: string) {
  const value = record[key]
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : []
}

function getNumberField(record: Record<string, unknown>, key: string) {
  const value = record[key]
  return typeof value === 'number' ? value : undefined
}
