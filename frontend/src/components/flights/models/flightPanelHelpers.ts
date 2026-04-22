import type { FlightSearchQueryDto } from '../../../lib/api-dtos/flights'
import type { FlightResponse } from '../../../lib/mvp-types/flights'
import type { FlightSearchState } from './flightTypes'

export function buildFlightSearchRequest(searchState: FlightSearchState): FlightSearchQueryDto {
  const primarySegment =
    searchState.tripType === 'multiCity' ? searchState.multiCitySegments[0] ?? null : null

  return {
    departureAirport:
      (searchState.tripType === 'multiCity' ? primarySegment?.departureAirport : searchState.departureAirport)?.trim() || undefined,
    arrivalAirport:
      (searchState.tripType === 'multiCity' ? primarySegment?.arrivalAirport : searchState.arrivalAirport)?.trim() || undefined,
    date: (searchState.tripType === 'multiCity' ? primarySegment?.departureDate : searchState.departureDate) || undefined,
  }
}

export function buildLateBookingNotice(
  flightResponse: FlightResponse,
  translate: (translationKey: string) => string,
): string {
  if (!flightResponse.lateBookingSurchargeAmount) {
    return translate('flights.surchargeDialogDescription')
  }

  return translate('flights.surchargeNoticeWithAmount')
    .replace('{amount}', flightResponse.lateBookingSurchargeAmount)
    .replace('{currency}', flightResponse.lateBookingSurchargeCurrency || flightResponse.currency)
}
