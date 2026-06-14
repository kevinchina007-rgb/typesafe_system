import { useCallback, useEffect, useMemo, useRef, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { consumeTourGroupBookingTarget } from '@/pages/shared/tour-group-booking/tourGroupBookingTarget'
import { loadFlightResultGroups, validateFlightSearchState } from '@/pages/FlightsPage/functions'
import { useFlightSearchState } from '../components/hooks/useFlightSearchState'
import type { FlightsPageController, FlightsPageProps } from '../objects'

// FlightsPage 的页面控制器，负责把搜索、预订、广告和状态恢复这些页面逻辑集中起来.
export function useFlightsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: FlightsPageProps): FlightsPageController & ReturnType<typeof useFlightSearchState> {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  const [lateBookingFlight, setLateBookingFlight] = useState<FlightPlannerResponse | null>(null)
  const [selectedTravelerIds, setSelectedTravelerIds] = useState<string[]>([])
  const [initialSelectedCabin, setInitialSelectedCabin] = useState<string | null>('all')
  const [isTourGroupTargetMode, setIsTourGroupTargetMode] = useState(false)
  const [targetFlightResponses, setTargetFlightResponses] = useState<FlightPlannerResponse[]>([])
  const [targetFlightResultGroups, setTargetFlightResultGroups] = useState<Array<{ id: string; title: string; subtitle: string; flightResponses: FlightPlannerResponse[] }>>([])
  const deliveryAdvertisements = useDeliverableAdvertisements('flightBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)
  const lastSubmittedSearchKey = useRef<string | null>(null)
  const lastReportedErrorKey = useRef<string | null>(null)
  const searchStateStore = useFlightSearchState()

  const searchKey = useMemo(() => JSON.stringify(searchStateStore.searchState), [searchStateStore.searchState])

  const reportErrorOnce = useCallback(
    (message: string) => {
      const nextErrorKey = `${searchKey}:${message}`
      if (lastReportedErrorKey.current === nextErrorKey) {
        return
      }

      lastReportedErrorKey.current = nextErrorKey
      onShowNotice('error', translate('error.friendly.default'), message)
    },
    [onShowNotice, searchKey, translate],
  )

  const searchFlights = useCallback(
    async (payload: Parameters<typeof travelMvpApiClient.searchFlightsPlanner>[0]) => {
      const flightListResponse = await travelMvpApiClient.searchFlightsPlanner(payload)
      return flightListResponse.flights
    },
    [],
  )

  const loadDailyLowestPrices = useCallback(
    (payload: Parameters<typeof travelMvpApiClient.flightDailyLowestPricesPlanner>[0]) =>
      travelMvpApiClient.flightDailyLowestPricesPlanner(payload),
    [],
  )

  useEffect(() => {
    void loadDeliverableAdvertisements('flightBooking')
    const reloadDeliverableAdvertisements = () => {
      void loadDeliverableAdvertisements('flightBooking')
    }

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        reloadDeliverableAdvertisements()
      }
    }

    window.addEventListener('focus', reloadDeliverableAdvertisements)
    document.addEventListener('visibilitychange', handleVisibilityChange)

    return () => {
      window.removeEventListener('focus', reloadDeliverableAdvertisements)
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [loadDeliverableAdvertisements])

  const handleOpenAdvertisement = useCallback(
    (advertisement: AdvertisementResponse) => {
      if (advertisement.targetResourceType === 'Flight' && advertisement.targetResourceId) {
        window.sessionStorage.setItem('flight-advertisement-target', advertisement.targetResourceId)
        onNavigate('flights')
        return
      }

      if (advertisement.landingTarget) {
        window.location.href = advertisement.landingTarget
      }
    },
    [onNavigate],
  )

  useEffect(() => {
    const target = consumeTourGroupBookingTarget('flights')
    if (target) {
      setIsTourGroupTargetMode(true)
      let cancelled = false

      void (async () => {
        try {
          const flight = await travelMvpApiClient.getFlightDetailsPlanner({ flightId: target.flightId })
          if (cancelled) {
            return
          }

          const departureDate = target.departureDate || flight.departureTime.slice(0, 10)
          const nextSearchState = {
            ...searchStateStore.searchState,
            tripType: 'oneWay' as const,
            departureAirport: target.departureAirport || flight.departureAirport,
            arrivalAirport: target.arrivalAirport || flight.arrivalAirport,
            departureDate,
            returnDate: '',
            multiCitySegments: searchStateStore.searchState.multiCitySegments,
          }

          searchStateStore.setSearchState(nextSearchState)
          searchStateStore.setFlightResultGroups([
            {
              id: 'tour-group-target',
              title: `${flight.departureAirport} -> ${flight.arrivalAirport}`,
              subtitle: flight.flightNumber,
              flightResponses: [flight],
            },
          ])
          searchStateStore.setFlightPlannerResponses([flight])
          searchStateStore.setHasSearchedFlights(true)
          setTargetFlightResponses([flight])
          setTargetFlightResultGroups([
            {
              id: 'tour-group-target',
              title: `${flight.departureAirport} -> ${flight.arrivalAirport}`,
              subtitle: flight.flightNumber,
              flightResponses: [flight],
            },
          ])
          setInitialSelectedCabin(target.cabinClass ?? 'all')
          lastSubmittedSearchKey.current = JSON.stringify(nextSearchState)
          window.scrollTo({ top: 0, behavior: 'smooth' })
        } catch (error) {
          if (!cancelled) {
          onShowNotice('error', translate('error.friendly.default'), error instanceof Error ? error.message : '航班广告跳转失败。')
          }
        }
      })()

      return () => {
        cancelled = true
      }
    }

    const targetFlightId = window.sessionStorage.getItem('flight-advertisement-target')
    if (!targetFlightId) {
      return
    }

    setIsTourGroupTargetMode(false)
    let cancelled = false

    void (async () => {
      try {
        const flight = await travelMvpApiClient.getFlightDetailsPlanner({ flightId: targetFlightId })
        if (cancelled) {
          return
        }

        const departureDate = flight.departureTime.slice(0, 10)
        const nextSearchState = {
          ...searchStateStore.searchState,
          tripType: 'oneWay' as const,
          departureAirport: flight.departureAirport,
          arrivalAirport: flight.arrivalAirport,
          departureDate,
          returnDate: '',
          multiCitySegments: searchStateStore.searchState.multiCitySegments,
        }

        searchStateStore.setSearchState(nextSearchState)
        searchStateStore.setFlightResultGroups([
          {
            id: 'advertisement-target',
            title: `${flight.departureAirport} -> ${flight.arrivalAirport}`,
            subtitle: flight.flightNumber,
            flightResponses: [flight],
          },
        ])
        searchStateStore.setFlightPlannerResponses([flight])
        searchStateStore.setHasSearchedFlights(true)
        setTargetFlightResponses([])
        setTargetFlightResultGroups([])
        setInitialSelectedCabin('all')
        lastSubmittedSearchKey.current = JSON.stringify(nextSearchState)
        window.sessionStorage.removeItem('flight-advertisement-target')
        window.scrollTo({ top: 0, behavior: 'smooth' })
      } catch (error) {
        if (!cancelled) {
          onShowNotice('error', translate('error.friendly.default'), error instanceof Error ? error.message : '航班广告跳转失败。')
        }
      }
    })()

    return () => {
      cancelled = true
    }
  }, [onShowNotice, searchStateStore, translate])

  useEffect(() => {
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  const submitSearch = useCallback(async () => {
    const validationMessage = validateFlightSearchState(searchStateStore.searchState)
    if (validationMessage) {
      reportErrorOnce(validationMessage)
      return
    }

    try {
      const nextResultGroups = await loadFlightResultGroups(searchStateStore.searchState, searchFlights)
      const nextFlights = nextResultGroups.flatMap(resultGroup => resultGroup.flightResponses)
      lastReportedErrorKey.current = null
      lastSubmittedSearchKey.current = searchKey
      setTargetFlightResponses([])
      setTargetFlightResultGroups([])
      searchStateStore.setFlightResultGroups(nextResultGroups)
      searchStateStore.setFlightPlannerResponses(nextFlights)
      searchStateStore.setHasSearchedFlights(nextResultGroups.length > 0)
    } catch {
      setTargetFlightResponses([])
      setTargetFlightResultGroups([])
      searchStateStore.setFlightResultGroups([])
      searchStateStore.setFlightPlannerResponses([])
      searchStateStore.setHasSearchedFlights(false)
      reportErrorOnce('航班接口出错了：请先确认后端已重启，并且新的航班演示数据已经迁移完成。')
    }
  }, [loadFlightResultGroups, reportErrorOnce, searchKey, searchFlights, searchStateStore])

  useEffect(() => {
    if (isTourGroupTargetMode) {
      return
    }

    if (!searchStateStore.hasSearchedFlights || lastSubmittedSearchKey.current === searchKey) {
      return
    }

    const timer = window.setTimeout(() => {
      void submitSearch()
    }, 250)

    return () => window.clearTimeout(timer)
  }, [isTourGroupTargetMode, searchStateStore.hasSearchedFlights, searchKey, submitSearch])

  const bookFlight = useCallback(
    async (payload: Parameters<typeof travelMvpApiClient.bookFlightPlanner>[0]) => {
      if (!signedInUser) {
        setIsAuthDialogOpen(true)
        return
      }

      await runPageAction(async () => {
        await travelMvpApiClient.bookFlightPlanner({
          userId: payload.userId,
          flightId: payload.flightId,
          travelerIds: payload.travelerIds,
          cabinClass: payload.cabinClass,
        })
        onNavigate('bookings')
      }, translate('flights.bookNow'), translate('notice.bookingCreated'))
    },
    [onNavigate, runPageAction, signedInUser, translate],
  )

  const toggleTravelerSelection = useCallback((travelerId: string) => {
    setSelectedTravelerIds(currentIds =>
      currentIds.includes(travelerId)
        ? currentIds.filter(nextTravelerId => nextTravelerId !== travelerId)
        : [...currentIds, travelerId],
    )
  }, [])

  return {
    ...searchStateStore,
    travelers,
    selectedTravelerIds,
    isBusy,
    isAuthDialogOpen,
    lateBookingFlight,
    signedInUserId: signedInUser?.userId ?? null,
    isGuestMode: signedInUser === null,
    isTourGroupTargetMode,
    targetFlightResponses,
    targetFlightResultGroups,
    deliveryAdvertisements,
    openAuthDialog: () => setIsAuthDialogOpen(true),
    closeAuthDialog: () => setIsAuthDialogOpen(false),
    openLateBookingReview: (flightResponse: FlightPlannerResponse) => setLateBookingFlight(flightResponse),
    closeLateBookingReview: () => setLateBookingFlight(null),
    searchFlights,
    loadDailyLowestPrices,
    bookFlight,
    submitSearch,
    toggleTravelerSelection,
    handleOpenAdvertisement,
    initialSelectedCabin,
  }
}

