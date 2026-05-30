import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightDailyLowestPricePlannerResponse, FlightDailyLowestPricesPlannerRequest, FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPrices'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import { getFlightDetailsPlannerAirlineDisplayNameByCode, getFlightDetailsPlannerAirlineLogoPathByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import { formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import { formatFlightAirportLabel, getFlightDetailsPlannerCityAirportCodes, normalizeFlightAirportForApi } from '@/app/stores/models/flights/flightConstants'

export type FlightSortMode = 'price' | 'departureTime'

export type FlightResultsRoute = {
  departureAirport: string
  arrivalAirport: string
  departureDate: string
}

export type DisplayFlight = {
  flight: FlightPlannerResponse
  airlineName: string
  airlineLogoPath: string | null
  departureAirportName: string
  arrivalAirportName: string
  displayCabinClass: string
  displayCabinLabel: string
  displayPrice: number
  displayCurrency: string
  isDisplayCabinBookable: boolean
  priceTone: 'lowest' | 'discount' | 'standard'
}

const cabinLabelByClass: Record<string, string> = {
  ECONOMY: '经济舱',
  PREMIUM_ECONOMY: '超级经济舱',
  BUSINESS: '商务舱',
  FIRST: '头等舱',
}

export const cabinOrder = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS', 'FIRST']

export const departureTimeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']

export function buildLateBookingNotice(
  flightResponse: FlightPlannerResponse,
  translate: (translationKey: string) => string,
): string {
  if (!flightResponse.lateBookingSurchargeAmount) {
    return translate('flights.surchargeDialogDescription')
  }

  return translate('flights.surchargeNoticeWithAmount')
    .replace('{amount}', flightResponse.lateBookingSurchargeAmount)
    .replace('{currency}', flightResponse.lateBookingSurchargeCurrency || flightResponse.currency)
}

export function loadFlightResultGroups(
  searchState: {
    tripType: 'oneWay' | 'roundTrip' | 'multiCity'
    departureAirport: string
    arrivalAirport: string
    departureDate: string
    returnDate: string
    multiCitySegments: Array<{
      id: string
      departureAirport: string
      arrivalAirport: string
      departureDate: string
      arrivalDate: string
    }>
  },
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>,
): Promise<Array<{ id: string; title: string; subtitle: string; flightResponses: FlightPlannerResponse[] }>> {
  const requests = buildFlightSearchRequests(searchState)
  if (requests.some(request => !request.query.departureAirport || !request.query.arrivalAirport || !request.query.date)) {
    return Promise.resolve([])
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

export function validateFlightSearchState(searchState: {
  tripType: 'oneWay' | 'roundTrip' | 'multiCity'
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  multiCitySegments: Array<{
    departureAirport: string
    arrivalAirport: string
    departureDate: string
  }>
}): string | null {
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
    return '往返小飞机还没等去程起飞呢：返回时间不能早于出发时间。'
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

export function loadDailyLowestPricesFromSearch(
  request: FlightDailyLowestPricesPlannerRequest,
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>,
): Promise<FlightDailyLowestPricesPlannerResponse> {
  const start = parseSearchDate(request.startDate)
  return Promise.all(
    Array.from({ length: request.days }, async (_, index) => {
      const date = new Date(start)
      date.setDate(start.getDate() + index)
      const dateText = formatDateInput(date)
      const flights = await onSearchFlights({
        departureAirport: request.departureAirport,
        arrivalAirport: request.arrivalAirport,
        date: dateText,
      })
      const priceValues = flights
        .flatMap(flight => flight.cabinInventories)
        .map(cabin => Number(cabin.unitPrice))
        .filter(Number.isFinite)
      const lowestPrice = priceValues.length > 0 ? Math.min(...priceValues) : null

      return {
        date: dateText,
        lowestPrice: lowestPrice === null ? null : String(lowestPrice),
        currency: lowestPrice === null ? null : 'CNY',
      }
    }),
  ).then(prices => ({ prices }))
}

export async function loadDailyLowestPricesAcrossAirportCodes(
  request: FlightDailyLowestPricesPlannerRequest,
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesPlannerRequest) => Promise<FlightDailyLowestPricesPlannerResponse>,
): Promise<FlightDailyLowestPricesPlannerResponse> {
  const departureAirportOptions = expandAirportSearchValues(request.departureAirport)
  const arrivalAirportOptions = expandAirportSearchValues(request.arrivalAirport)

  if (departureAirportOptions.length <= 1 && arrivalAirportOptions.length <= 1) {
    return onLoadDailyLowestPrices(request)
  }

  const responses = await Promise.all(
    departureAirportOptions.flatMap(departureAirport =>
      arrivalAirportOptions.map(arrivalAirport =>
        onLoadDailyLowestPrices({
          ...request,
          departureAirport,
          arrivalAirport,
        }),
      ),
    ),
  )

  const pricesByDate = new Map<string, FlightDailyLowestPricePlannerResponse>()
  responses.flatMap(response => response.prices).forEach(price => {
    const currentPrice = pricesByDate.get(price.date)
    const nextAmount = price.lowestPrice === null ? null : Number(price.lowestPrice)
    const currentAmount = currentPrice?.lowestPrice === null || currentPrice?.lowestPrice === undefined ? null : Number(currentPrice.lowestPrice)

    if (!currentPrice || (nextAmount !== null && (currentAmount === null || nextAmount < currentAmount))) {
      pricesByDate.set(price.date, price)
    }
  })

  return { prices: [...pricesByDate.values()].sort((left, right) => left.date.localeCompare(right.date)) }
}

export function expandAirportSearchValues(value: string | undefined): string[] {
  if (!value) {
    return []
  }

  const airportCodes = getFlightDetailsPlannerCityAirportCodes(value)
  if (airportCodes.length > 0) {
    return airportCodes
  }

  return [normalizeFlightAirportForApi(value) ?? value]
}

export function buildEmptyDateWindow(startDate: string, days: number): FlightDailyLowestPricePlannerResponse[] {
  const start = parseSearchDate(startDate)
  return Array.from({ length: days }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return {
      date: formatDateInput(date),
      lowestPrice: null,
      currency: null,
    }
  })
}

export function formatAirportName(value: string): string {
  return formatFlightAirportLabel(value)
}

export function formatCabinLabel(value: string): string {
  return cabinLabelByClass[normalizeCabinClass(value)] ?? value
}

export function getAirlineDisplayName(flight: FlightPlannerResponse): string {
  return getFlightDetailsPlannerAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)
}

export function getAirlineLogoPath(flight: FlightPlannerResponse): string | null {
  return getFlightDetailsPlannerAirlineLogoPathByCode(flight.airlineCode, flight.airlineLogoPath)
}

export function normalizeCabinClass(value: string): string {
  return value.trim().replace('-', '_').toUpperCase()
}

export function unique(values: string[]): string[] {
  return [...new Set(values.filter(Boolean))]
}

export function parseSearchDate(value: string): Date {
  if (!value) {
    return new Date()
  }
  return new Date(`${value}T00:00:00`)
}

export function formatDateInput(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatRouteDate(date: string): string {
  if (!date) {
    return ''
  }

  return new Date(`${date}T00:00:00`).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    weekday: 'short',
  })
}

export function toDisplayFlight(flight: FlightPlannerResponse, selectedCabin: string): DisplayFlight {
  const selectedInventory =
    selectedCabin === 'all'
      ? [...flight.cabinInventories].sort((left, right) => Number(left.unitPrice) - Number(right.unitPrice))[0]
      : flight.cabinInventories.find(cabin => normalizeCabinClass(cabin.cabinClass) === selectedCabin)
  const cabinClass = normalizeCabinClass(selectedInventory?.cabinClass ?? 'ECONOMY')
  const price = Number(selectedInventory?.unitPrice ?? flight.basePrice)

  return {
    flight,
    airlineName: getAirlineDisplayName(flight),
    airlineLogoPath: getAirlineLogoPath(flight),
    departureAirportName: formatAirportName(flight.departureAirport),
    arrivalAirportName: formatAirportName(flight.arrivalAirport),
    displayCabinClass: cabinClass,
    displayCabinLabel: formatCabinLabel(cabinClass),
    displayPrice: price,
    displayCurrency: selectedInventory?.currency ?? flight.currency,
    isDisplayCabinBookable: Boolean(selectedInventory?.isBookable),
    priceTone: 'standard',
  }
}

function buildFlightSearchRequests(searchState: {
  tripType: 'oneWay' | 'roundTrip' | 'multiCity'
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  multiCitySegments: Array<{
    id: string
    departureAirport: string
    arrivalAirport: string
    departureDate: string
    arrivalDate: string
  }>
}): Array<{ id: string; title: string; subtitle: string; query: FlightSearchPlannerRequest }> {
  if (searchState.tripType === 'multiCity') {
    return searchState.multiCitySegments.map((segment, index) => ({
      id: segment.id,
      title: `第${index + 1}程`,
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
      query: {
        departureAirport: normalizeFlightAirportForApi(searchState.departureAirport),
        arrivalAirport: normalizeFlightAirportForApi(searchState.arrivalAirport),
        date: searchState.departureDate || undefined,
      },
    },
  ]
}

async function searchFlightsAcrossAirportCodes(
  query: FlightSearchPlannerRequest,
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>,
): Promise<FlightPlannerResponse[]> {
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

function uniqueFlights(flights: FlightPlannerResponse[]): FlightPlannerResponse[] {
  const seenFlightIds = new Set<string>()
  return flights.filter(flight => {
    if (seenFlightIds.has(flight.flightId)) {
      return false
    }
    seenFlightIds.add(flight.flightId)
    return true
  })
}
