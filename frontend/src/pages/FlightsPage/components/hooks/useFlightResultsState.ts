import { useEffect, useMemo, useState } from 'react'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import type { FlightDailyLowestPricePlannerResponse, FlightDailyLowestPricesPlannerRequest, FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPrices'
import {
  buildEmptyDateWindow,
  cabinOrder,
  formatAirportName,
  formatCabinLabel,
  formatRouteDate,
  loadDailyLowestPricesAcrossAirportCodes,
  loadDailyLowestPricesFromSearch,
  normalizeCabinClass,
  parseSearchDate,
  unique,
  formatDateInput,
  toDisplayFlight,
} from '@/pages/FlightsPage/functions'
import type { FlightResultsRoute, FlightSortMode } from '@/pages/FlightsPage/objects'

export function useFlightResultsState({
  route,
  searchedFlights,
  hasSearchedFlights,
  theme,
  onSearchFlights,
  onLoadDailyLowestPrices,
  initialSelectedCabin = 'all',
}: {
  route: FlightResultsRoute
  searchedFlights: FlightPlannerResponse[]
  hasSearchedFlights: boolean
  theme: 'outbound' | 'return' | 'single'
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesPlannerRequest) => Promise<FlightDailyLowestPricesPlannerResponse>
  initialSelectedCabin?: string | null
}) {
  const [selectedAirline, setSelectedAirline] = useState('all')
  const [selectedTimeRange, setSelectedTimeRange] = useState('all')
  const [selectedDepartureAirport, setSelectedDepartureAirport] = useState('all')
  const [selectedArrivalAirport, setSelectedArrivalAirport] = useState('all')
  const [selectedCabin, setSelectedCabin] = useState('all')
  const [sortMode, setSortMode] = useState<FlightSortMode>('price')
  const [dateWindowOffset, setDateWindowOffset] = useState(-3)
  const [dailyLowestPrices, setDailyLowestPrices] = useState<FlightDailyLowestPricePlannerResponse[]>([])

  useEffect(() => {
    setSelectedAirline('all')
    setSelectedTimeRange('all')
    setSelectedDepartureAirport('all')
    setSelectedArrivalAirport('all')
    setSelectedCabin(initialSelectedCabin ?? 'all')
    setSortMode('price')
    setDateWindowOffset(-3)
  }, [initialSelectedCabin, theme])

  const airlineOptions = useMemo(
    () => unique(searchedFlights.map(flight => flight.airlineName || flight.airlineCode)),
    [searchedFlights],
  )

  const departureAirportOptions = useMemo(
    () => unique(searchedFlights.map(flight => flight.departureAirport)),
    [searchedFlights],
  )

  const arrivalAirportOptions = useMemo(
    () => unique(searchedFlights.map(flight => flight.arrivalAirport)),
    [searchedFlights],
  )

  const cabinOptions = useMemo(
    () => unique(searchedFlights.flatMap(flight => flight.cabinInventories.map(cabin => normalizeCabinClass(cabin.cabinClass))))
      .sort((left, right) => cabinOrder.indexOf(left) - cabinOrder.indexOf(right)),
    [searchedFlights],
  )

  const displayFlights = useMemo(() => {
    const filtered = searchedFlights.filter(flight => {
      const departureHour = new Date(flight.departureTime).getHours()
      const matchesAirline = selectedAirline === 'all' || (flight.airlineName || flight.airlineCode) === selectedAirline
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
      departureAirport: route.departureAirport,
      arrivalAirport: route.arrivalAirport,
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
