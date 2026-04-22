import type { AppLanguage, FlightResponse, TravelerResponse } from '../../../lib/mvp-types'
import { localizeCabinClass, mapBackendStatusToProductLabel } from '../../../lib/view-models'
import type { FlightSearchSegment, QuickDatePreset } from './flightTypes'

export function renderFlightTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function formatFlightDuration(departureTime: string, arrivalTime: string) {
  const durationMs = new Date(arrivalTime).getTime() - new Date(departureTime).getTime()
  const totalMinutes = Math.max(0, Math.round(durationMs / 60000))
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  return `${hours}h ${minutes}m`
}

export function getLowestPriceLabel(flights: FlightResponse[], translate: (translationKey: string) => string) {
  const candidatePrices = flights.flatMap(flight =>
    [Number(flight.basePrice), ...flight.cabinInventories.map(cabinInventory => Number(cabinInventory.unitPrice))].filter(
      value => Number.isFinite(value),
    ),
  )

  if (candidatePrices.length === 0) {
    return translate('flights.priceInsightFallback')
  }

  return translate('flights.priceInsight').replace('{price}', String(Math.min(...candidatePrices)))
}

export function getSuggestedTravelWindowLabel(flights: FlightResponse[], translate: (translationKey: string) => string) {
  if (flights.length === 0) {
    return translate('flights.bestWindowFallback')
  }

  const earliestFlight = [...flights].sort(
    (left, right) => new Date(left.departureTime).getTime() - new Date(right.departureTime).getTime(),
  )[0]

  const hour = new Date(earliestFlight.departureTime).getHours()
  if (hour < 10) {
    return translate('flights.bestWindowMorning')
  }
  if (hour < 16) {
    return translate('flights.bestWindowMidday')
  }
  return translate('flights.bestWindowEvening')
}

export function getQuickDatePresetValue(
  preset: QuickDatePreset,
  tripType: 'oneWay' | 'roundTrip' | 'multiCity',
  baseDate = new Date(),
) {
  const nextDate = new Date(baseDate)

  if (preset === 'tomorrow') {
    nextDate.setDate(baseDate.getDate() + 1)
  } else if (preset === 'weekend') {
    const day = baseDate.getDay()
    const offset = day === 6 ? 0 : day === 0 ? 6 : 6 - day
    nextDate.setDate(baseDate.getDate() + offset)
  } else if (preset === 'nextWeek') {
    nextDate.setDate(baseDate.getDate() + 7)
  }

  const departureDate = nextDate.toISOString().slice(0, 10)
  if (tripType !== 'roundTrip') {
    return { departureDate }
  }

  const returnDate = new Date(nextDate)
  returnDate.setDate(nextDate.getDate() + 4)
  return {
    departureDate,
    returnDate: returnDate.toISOString().slice(0, 10),
  }
}

export function getQuickDatePresetSegments(
  preset: QuickDatePreset,
  currentSegments: FlightSearchSegment[],
  baseDate = new Date(),
): FlightSearchSegment[] {
  const { departureDate } = getQuickDatePresetValue(preset, 'oneWay', baseDate)

  return currentSegments.map((segment, index) => {
    const nextDate = new Date(`${departureDate}T00:00:00`)
    nextDate.setDate(nextDate.getDate() + index * 2)
    const shiftedDate = nextDate.toISOString().slice(0, 10)
    return {
      ...segment,
      departureDate: shiftedDate,
      arrivalDate: shiftedDate,
    }
  })
}

export function getFlightStatusLabel(status: string, currentLanguage: AppLanguage) {
  return mapBackendStatusToProductLabel(status, currentLanguage)
}

export function getLocalizedCabinLabel(cabinClass: string, currentLanguage: AppLanguage) {
  return localizeCabinClass(cabinClass, currentLanguage)
}
