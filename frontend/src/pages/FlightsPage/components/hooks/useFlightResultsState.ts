import { useEffect, useMemo, useState } from 'react'

import type { FlightResponse } from '@/lib/mvp-types/flights'
import type { FlightSearchQuery } from '@/microservices/flight/objects/FlightSearchQuery'
import { getFlightAirlineDisplayNameByCode, getFlightAirlineLogoPathByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import { formatFlightAirportLabel, getFlightCityAirportCodes, normalizeFlightAirportForApi } from '@/app/stores/models/flights/flightConstants'
import type {
  FlightDailyLowestPriceResponse,
  FlightDailyLowestPricesRequest,
  FlightDailyLowestPricesResponse,
} from '@/microservices/flight/objects/FlightDailyLowestPrices'

export type FlightSortMode = 'price' | 'departureTime'

export type FlightResultsRoute = {
  departureAirport: string
  arrivalAirport: string
  departureDate: string
}

export type DisplayFlight = {
  flight: FlightResponse
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

const cabinOrder = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS', 'FIRST']

export const departureTimeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']

export function useFlightResultsState({
  route,
  searchedFlights,
  hasSearchedFlights,
  theme,
  onSearchFlights,
  onLoadDailyLowestPrices,
}: {
  route: FlightResultsRoute
  searchedFlights: FlightResponse[]
  hasSearchedFlights: boolean
  theme: 'outbound' | 'return' | 'single'
  onSearchFlights: (payload: FlightSearchQuery) => Promise<FlightResponse[]>
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesRequest) => Promise<FlightDailyLowestPricesResponse>
}) {
  const [selectedAirline, setSelectedAirline] = useState('all')
  const [selectedTimeRange, setSelectedTimeRange] = useState('all')
  const [selectedDepartureAirport, setSelectedDepartureAirport] = useState('all')
  const [selectedArrivalAirport, setSelectedArrivalAirport] = useState('all')
  const [selectedCabin, setSelectedCabin] = useState('all')
  const [sortMode, setSortMode] = useState<FlightSortMode>('price')
  const [dateWindowOffset, setDateWindowOffset] = useState(-3)
  const [dailyLowestPrices, setDailyLowestPrices] = useState<FlightDailyLowestPriceResponse[]>([])

  useEffect(() => {
    setSelectedAirline('all')
    setSelectedTimeRange('all')
    setSelectedDepartureAirport('all')
    setSelectedArrivalAirport('all')
    setSelectedCabin('all')
    setSortMode('price')
    setDateWindowOffset(-3)
  }, [theme])

  const airlineOptions = useMemo(
    () => unique(searchedFlights.map(flight => getAirlineDisplayName(flight))),
    [searchedFlights],
  )

  const departureAirportOptions = useMemo(
    () => getFlightCityAirportCodes(route.departureAirport),
    [route.departureAirport],
  )

  const arrivalAirportOptions = useMemo(
    () => getFlightCityAirportCodes(route.arrivalAirport),
    [route.arrivalAirport],
  )

  const cabinOptions = useMemo(
    () => unique(searchedFlights.flatMap(flight => flight.cabinInventories.map(cabin => normalizeCabinClass(cabin.cabinClass))))
      .sort((left, right) => cabinOrder.indexOf(left) - cabinOrder.indexOf(right)),
    [searchedFlights],
  )

  const displayFlights = useMemo(() => {
    const filtered = searchedFlights.filter(flight => {
      const departureHour = new Date(flight.departureTime).getHours()
      const matchesAirline = selectedAirline === 'all' || getAirlineDisplayName(flight) === selectedAirline
      const matchesDepartureAirport = selectedDepartureAirport === 'all' || flight.departureAirport === selectedDepartureAirport
      const matchesArrivalAirport = selectedArrivalAirport === 'all' || flight.arrivalAirport === selectedArrivalAirport
      const matchesCabin =
        selectedCabin === 'all' ||
        flight.cabinInventories.some(cabin => normalizeCabinClass(cabin.cabinClass) === selectedCabin)

      if (selectedTimeRange === 'all') {
        return matchesAirline && matchesDepartureAirport && matchesArrivalAirport && matchesCabin
      }

      const [startHour, endHour] = selectedTimeRange.split('-').map(value => Number(value.slice(0, 2)))
      return matchesAirline && matchesDepartureAirport && matchesArrivalAirport && matchesCabin && departureHour >= startHour && departureHour <= endHour
    })

    const displayRows = filtered.map(flight => toDisplayFlight(flight, selectedCabin)).filter(row => Number.isFinite(row.displayPrice))
    const lowestVisiblePrice = displayRows.length > 0 ? Math.min(...displayRows.map(row => row.displayPrice)) : null
    const tonedRows = displayRows.map(row => ({
      ...row,
      priceTone: lowestVisiblePrice !== null && row.displayPrice === lowestVisiblePrice
        ? 'lowest' as const
        : row.displayPrice < Number(row.flight.basePrice)
          ? 'discount' as const
          : 'standard' as const,
    }))

    return [...tonedRows].sort((left, right) => {
      if (sortMode === 'departureTime') {
        return new Date(left.flight.departureTime).getTime() - new Date(right.flight.departureTime).getTime()
      }

      return left.displayPrice - right.displayPrice
    })
  }, [searchedFlights, selectedAirline, selectedArrivalAirport, selectedCabin, selectedDepartureAirport, selectedTimeRange, sortMode])

  const dateWindowStart = useMemo(() => {
    const date = parseSearchDate(route.departureDate)
    date.setDate(date.getDate() + dateWindowOffset)
    return formatDateInput(date)
  }, [dateWindowOffset, route.departureDate])

  useEffect(() => {
    if (!hasSearchedFlights || !route.departureAirport || !route.arrivalAirport || !dateWindowStart) {
      setDailyLowestPrices([])
      return
    }

    let isCurrent = true
    const request = {
      departureAirport: normalizeFlightAirportForApi(route.departureAirport) ?? route.departureAirport,
      arrivalAirport: normalizeFlightAirportForApi(route.arrivalAirport) ?? route.arrivalAirport,
      startDate: dateWindowStart,
      days: 7,
    }

    loadDailyLowestPricesAcrossAirportCodes(request, onLoadDailyLowestPrices)
      .catch(() => loadDailyLowestPricesFromSearch(request, onSearchFlights))
      .then(response => {
        if (isCurrent) {
          setDailyLowestPrices(response.prices)
        }
      })
      .catch(() => {
        if (isCurrent) {
          setDailyLowestPrices([])
        }
      })

    return () => {
      isCurrent = false
    }
  }, [dateWindowStart, hasSearchedFlights, onLoadDailyLowestPrices, onSearchFlights, route.arrivalAirport, route.departureAirport])

  return {
    selectedAirline,
    selectedTimeRange,
    selectedDepartureAirport,
    selectedArrivalAirport,
    selectedCabin,
    sortMode,
    airlineOptions,
    departureAirportOptions,
    arrivalAirportOptions,
    cabinOptions,
    displayFlights,
    dailyLowestPrices: dailyLowestPrices.length > 0 ? dailyLowestPrices : buildEmptyDateWindow(dateWindowStart, 7),
    setSelectedAirline,
    setSelectedTimeRange,
    setSelectedDepartureAirport,
    setSelectedArrivalAirport,
    setSelectedCabin,
    setSortMode,
    showPreviousDateWindow: () => setDateWindowOffset(offset => offset - 1),
    showNextDateWindow: () => setDateWindowOffset(offset => offset + 1),
    formatAirportName,
    formatCabinLabel,
    formatRouteDate,
  }
}

export function formatAirportName(value: string): string {
  return formatFlightAirportLabel(value)
}

export function formatCabinLabel(value: string): string {
  return cabinLabelByClass[normalizeCabinClass(value)] ?? value
}

export function getAirlineDisplayName(flight: FlightResponse): string {
  return getFlightAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)
}

export function getAirlineLogoPath(flight: FlightResponse): string | null {
  return getFlightAirlineLogoPathByCode(flight.airlineCode, flight.airlineLogoPath)
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

async function loadDailyLowestPricesFromSearch(
  request: FlightDailyLowestPricesRequest,
  onSearchFlights: (payload: FlightSearchQuery) => Promise<FlightResponse[]>,
): Promise<FlightDailyLowestPricesResponse> {
  const start = parseSearchDate(request.startDate)
  const prices = await Promise.all(
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
  )

  return { prices }
}

async function loadDailyLowestPricesAcrossAirportCodes(
  request: FlightDailyLowestPricesRequest,
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesRequest) => Promise<FlightDailyLowestPricesResponse>,
): Promise<FlightDailyLowestPricesResponse> {
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

  const pricesByDate = new Map<string, FlightDailyLowestPriceResponse>()
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

function toDisplayFlight(flight: FlightResponse, selectedCabin: string): DisplayFlight {
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

function normalizeCabinClass(value: string): string {
  return value.trim().replace('-', '_').toUpperCase()
}

function unique(values: string[]): string[] {
  return [...new Set(values.filter(Boolean))]
}

function parseSearchDate(value: string): Date {
  if (!value) {
    return new Date()
  }
  return new Date(`${value}T00:00:00`)
}

function formatDateInput(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildEmptyDateWindow(startDate: string, days: number): FlightDailyLowestPriceResponse[] {
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
