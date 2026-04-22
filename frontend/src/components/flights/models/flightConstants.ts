import type { HotRoute } from '../controls/HotRoutes'
import type { FlightSearchSegment, FlightSearchState } from './flightTypes'

export function createFlightSearchSegment(
  id: string,
  departureAirport: string,
  arrivalAirport: string,
  departureDate: string,
  arrivalDate = departureDate,
): FlightSearchSegment {
  return {
    id,
    departureAirport,
    arrivalAirport,
    departureDate,
    arrivalDate,
  }
}

export const defaultFlightSearchState: FlightSearchState = {
  tripType: 'oneWay',
  departureAirport: '上海',
  arrivalAirport: '东京',
  departureDate: '2026-04-20',
  returnDate: '2026-04-27',
  selectedQuickDatePreset: null,
  multiCitySegments: [
    createFlightSearchSegment('segment-1', '上海', '东京', '2026-04-20'),
    createFlightSearchSegment('segment-2', '东京', '香港', '2026-04-24'),
  ],
  adults: 1,
  childrenCount: 0,
  cabinPreference: 'Economy',
}

export const flightHotRoutes: HotRoute[] = [
  { id: 'pek-sha', departureLabel: '\u5317\u4eac', arrivalLabel: '\u4e0a\u6d77' },
  { id: 'sha-tyo', departureLabel: '\u4e0a\u6d77', arrivalLabel: '\u4e1c\u4eac' },
  { id: 'can-sin', departureLabel: '\u5e7f\u5dde', arrivalLabel: '\u65b0\u52a0\u5761' },
  { id: 'sha-hkg', departureLabel: '\u4e0a\u6d77', arrivalLabel: '\u9999\u6e2f' },
]

export const recentFlightSearches = [
  '\u4e0a\u6d77 / \u6d66\u4e1c',
  '\u4e1c\u4eac / \u6210\u7530',
  '\u5317\u4eac / \u9996\u90fd',
  '\u65b0\u52a0\u5761 / \u6a1f\u5b9c',
]

export const popularFlightCities = [
  '\u4e0a\u6d77',
  '\u5317\u4eac',
  '\u4e1c\u4eac',
  '\u9999\u6e2f',
  '\u65b0\u52a0\u5761',
  '\u5e7f\u5dde',
]
