import type { FlightResponse } from '@/lib/mvp-types/flights'
import type { FlightSearchQuery } from '@/microservices/flight/objects/FlightSearchQuery'
import type { FlightResultGroup, FlightSearchState } from '@/app/stores/models/flights/flightTypes'
import { formatFlightRouteCity, getFlightCityAirportCodes, normalizeFlightAirportForApi } from '@/app/stores/models/flights/flightConstants'

export function buildFlightSearchRequest(searchState: FlightSearchState): FlightSearchQuery {
  const primarySegment = searchState.tripType === 'multiCity' ? searchState.multiCitySegments[0] ?? null : null

  return {
    departureAirport: normalizeFlightAirportForApi(searchState.tripType === 'multiCity' ? primarySegment?.departureAirport : searchState.departureAirport),
    arrivalAirport: normalizeFlightAirportForApi(searchState.tripType === 'multiCity' ? primarySegment?.arrivalAirport : searchState.arrivalAirport),
    date: (searchState.tripType === 'multiCity' ? primarySegment?.departureDate : searchState.departureDate) || undefined,
  }
}

export function buildFlightSearchRequests(
  searchState: FlightSearchState,
): Array<{ id: string; title: string; subtitle: string; query: FlightSearchQuery }> {
  if (searchState.tripType === 'multiCity') {
    return searchState.multiCitySegments.map((segment, index) => ({
      id: segment.id,
      title: `第 ${index + 1} 程`,
      subtitle: `${formatFlightRouteCity(segment.departureAirport)} → ${formatFlightRouteCity(segment.arrivalAirport)} ${segment.departureDate}`,
      query: {
        departureAirport: normalizeFlightAirportForApi(segment.departureAirport),
        arrivalAirport: normalizeFlightAirportForApi(segment.arrivalAirport),
        date: segment.departureDate || undefined,
      },
    }))
  }

  if (searchState.tripType === 'roundTrip') {
    return [
      {
        id: 'outbound',
        title: '去程',
        subtitle: `${formatFlightRouteCity(searchState.departureAirport)} → ${formatFlightRouteCity(searchState.arrivalAirport)} ${searchState.departureDate}`,
        query: {
          departureAirport: normalizeFlightAirportForApi(searchState.departureAirport),
          arrivalAirport: normalizeFlightAirportForApi(searchState.arrivalAirport),
          date: searchState.departureDate || undefined,
        },
      },
      {
        id: 'return',
        title: '返程',
        subtitle: `${formatFlightRouteCity(searchState.arrivalAirport)} → ${formatFlightRouteCity(searchState.departureAirport)} ${searchState.returnDate}`,
        query: {
          departureAirport: normalizeFlightAirportForApi(searchState.arrivalAirport),
          arrivalAirport: normalizeFlightAirportForApi(searchState.departureAirport),
          date: searchState.returnDate || undefined,
        },
      },
    ]
  }

  return [
    {
      id: 'one-way',
      title: '单程',
      subtitle: `${formatFlightRouteCity(searchState.departureAirport)} → ${formatFlightRouteCity(searchState.arrivalAirport)} ${searchState.departureDate}`,
      query: buildFlightSearchRequest(searchState),
    },
  ]
}

export async function loadFlightResultGroups(
  searchState: FlightSearchState,
  onSearchFlights: (payload: FlightSearchQuery) => Promise<FlightResponse[]>,
): Promise<FlightResultGroup[]> {
  const requests = buildFlightSearchRequests(searchState)
  if (requests.some(request => !request.query.departureAirport || !request.query.arrivalAirport || !request.query.date)) {
    return []
  }

  return Promise.all(
    requests.map(async request => ({
      id: request.id,
      title: request.title,
      subtitle: request.subtitle,
      flightResponses: await searchFlightsAcrossAirportCodes(request.query, onSearchFlights),
    })),
  )
}

async function searchFlightsAcrossAirportCodes(
  query: FlightSearchQuery,
  onSearchFlights: (payload: FlightSearchQuery) => Promise<FlightResponse[]>,
): Promise<FlightResponse[]> {
  const departureAirportOptions = expandAirportSearchValues(query.departureAirport)
  const arrivalAirportOptions = expandAirportSearchValues(query.arrivalAirport)

  if (departureAirportOptions.length <= 1 && arrivalAirportOptions.length <= 1) {
    return onSearchFlights(query)
  }

  const responses = await Promise.all(
    departureAirportOptions.flatMap(departureAirport =>
      arrivalAirportOptions.map(arrivalAirport =>
        onSearchFlights({
          ...query,
          departureAirport,
          arrivalAirport,
        }),
      ),
    ),
  )
  return uniqueFlights(responses.flat())
}

function expandAirportSearchValues(value: string | undefined): string[] {
  if (!value) {
    return []
  }

  const airportCodes = getFlightCityAirportCodes(value)
  if (airportCodes.length > 0) {
    return airportCodes
  }

  return [normalizeFlightAirportForApi(value) ?? value]
}

function uniqueFlights(flights: FlightResponse[]): FlightResponse[] {
  const seenFlightIds = new Set<string>()
  return flights.filter(flight => {
    if (seenFlightIds.has(flight.flightId)) {
      return false
    }
    seenFlightIds.add(flight.flightId)
    return true
  })
}

export function validateFlightSearchState(searchState: FlightSearchState): string | null {
  const routes =
    searchState.tripType === 'multiCity'
      ? searchState.multiCitySegments
      : [{ departureAirport: searchState.departureAirport, arrivalAirport: searchState.arrivalAirport }]

  if (
    routes.some(route => {
      const departureAirport = route.departureAirport.trim()
      const arrivalAirport = route.arrivalAirport.trim()
      return !departureAirport || !arrivalAirport
    })
  ) {
    return '小飞机找不到跑道啦：请先选好出发地和目的地。'
  }

  if (
    (searchState.tripType === 'multiCity' && searchState.multiCitySegments.some(segment => !segment.departureDate.trim())) ||
    (searchState.tripType !== 'multiCity' && !searchState.departureDate.trim()) ||
    (searchState.tripType === 'roundTrip' && !searchState.returnDate.trim())
  ) {
    return '小日历还没翻开呢：请先选好出发日期。'
  }

  if (
    routes.some(route => {
      const departureAirport = route.departureAirport.trim()
      const arrivalAirport = route.arrivalAirport.trim()
      return departureAirport === arrivalAirport
    })
  ) {
    return '小飞机原地转圈啦：出发地和目的地不能选成同一个地方。'
  }

  if (
    searchState.tripType === 'roundTrip' &&
    searchState.departureDate &&
    searchState.returnDate &&
    searchState.returnDate < searchState.departureDate
  ) {
    return '返程小飞机还没等去程起飞呢：返回时间不能早于出发时间。'
  }

  if (searchState.tripType === 'multiCity') {
    for (let index = 1; index < searchState.multiCitySegments.length; index += 1) {
      const previousDate = searchState.multiCitySegments[index - 1]?.departureDate
      const nextDate = searchState.multiCitySegments[index]?.departureDate
      if (previousDate && nextDate && nextDate < previousDate) {
        return '行程时间线打结啦：下一程时间不能早于上一程时间。'
      }
    }
  }

  return null
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
