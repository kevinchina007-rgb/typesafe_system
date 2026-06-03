import { useCallback, useEffect, useMemo, useRef, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { addHotelDays } from '@/app/stores/models/hotel-booking-model'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { consumeTourGroupBookingTarget } from '@/pages/shared/tour-group-booking/tourGroupBookingTarget'
import type { HotelPlannerResponse } from '@/microservices/hotel/objects/HotelResponse'
import type {
  HotelBookRequest,
  HotelSearchNotice,
  HotelsPageController,
  HotelsPageProps,
} from '../objects'
import { formatHotelSearchRequest, getLowestRoomPrice, validateHotelSearchInput } from '../functions'
import { useHotelSearchState } from '../components/hooks/useHotelSearchState'

function normalizeDateOnly(value: string) {
  return value.trim().slice(0, 10)
}

export function useHotelsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: HotelsPageProps): HotelsPageController {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  const {
    hotelResponses,
    hasSearchedHotels,
    searchLocation,
    searchCheckInDate,
    searchCheckOutDate,
    roomCount,
    hotelPreference,
    nearbyPreference,
    setHotelPlannerResponses,
    setHasSearchedHotels,
    setSearchLocation,
    setSearchCheckInDate,
    setSearchCheckOutDate,
    setHotelPreference,
    setNearbyPreference,
  } = useHotelSearchState()
  const [selectedTravelerIds, setSelectedTravelerIds] = useState<string[]>([])
  const [isTourGroupTargetMode, setIsTourGroupTargetMode] = useState(false)
  const [targetHotelResponses, setTargetHotelResponses] = useState<HotelPlannerResponse[]>([])

  const deliveryAdvertisements = useDeliverableAdvertisements('hotelBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)
  const [selectedAdvertisement, setSelectedAdvertisement] = useState<AdvertisementResponse | null>(null)
  const [searchNotice, setSearchNotice] = useState<HotelSearchNotice | null>(null)
  const [searchRevision, setSearchRevision] = useState(0)
  const [dateWindowStart, setDateWindowStart] = useState(() => addHotelDays(searchCheckInDate, -3))
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  const resultsSectionRef = useRef<HTMLDivElement | null>(null)
  const noticeSectionRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    void loadDeliverableAdvertisements('hotelBooking')

    const reloadDeliverableAdvertisements = () => {
      void loadDeliverableAdvertisements('hotelBooking')
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

  useEffect(() => {
    const target = consumeTourGroupBookingTarget('hotels')
    if (!target) {
      return
    }

    setIsTourGroupTargetMode(true)
    let cancelled = false

    void (async () => {
      try {
        const checkInDate = normalizeDateOnly(target.checkInDate)
        const checkOutDate = normalizeDateOnly(target.checkOutDate)
        const nextHotelPlannerResponse = await travelMvpApiClient.getHotelDetailsPlanner(target.hotelId, {
          checkInDate,
          checkOutDate,
        })
        if (cancelled) {
          return
        }

        const nextHotelResponse = {
          ...nextHotelPlannerResponse,
          roomTypes: nextHotelPlannerResponse.roomTypes.filter(roomType => roomType.roomTypeId === target.roomTypeId),
        }

        setSearchLocation(nextHotelPlannerResponse.location)
        setSearchCheckInDate(checkInDate)
        setSearchCheckOutDate(checkOutDate)
        setHasSearchedHotels(true)
        setTargetHotelResponses([nextHotelResponse])
        setHotelPlannerResponses([nextHotelResponse])
        setSearchNotice(null)
        setDateWindowStart(addHotelDays(checkInDate, -3))
        setSearchRevision(revision => revision + 1)
        window.scrollTo({ top: 0, behavior: 'smooth' })
      } catch (error) {
        if (!cancelled) {
          onShowNotice('error', translate('error.friendly.default'), error instanceof Error ? error.message : translate('error.friendly.default'))
        }
      }
    })()

    return () => {
      cancelled = true
    }
  }, [onShowNotice, translate])

  useEffect(() => {
    setDateWindowStart(addHotelDays(searchCheckInDate, -3))
  }, [searchCheckInDate])

  useEffect(() => {
    if (searchRevision <= 0) {
      return
    }

    resultsSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [searchRevision])

  useEffect(() => {
    if (!searchNotice) {
      return
    }

    noticeSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, [searchNotice])

  useEffect(() => {
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  const featuredAdvertisement = deliveryAdvertisements[0] ?? null
  const hotelLowestNightlyPrice = useMemo(() => getLowestRoomPrice(hotelResponses), [hotelResponses])

  const toggleTravelerSelection = useCallback((travelerId: string) => {
    setSelectedTravelerIds(currentIds =>
      currentIds.includes(travelerId)
        ? currentIds.filter(nextTravelerId => nextTravelerId !== travelerId)
        : [...currentIds, travelerId],
    )
  }, [])

  async function executeHotelSearch(nextLocation: string, nextCheckInDate: string, nextCheckOutDate: string) {
    const validationNotice = validateHotelSearchInput(translate, nextLocation, nextCheckInDate, nextCheckOutDate)
    if (validationNotice) {
      setSearchNotice(validationNotice)
      onShowNotice('error', translate('error.friendly.default'), validationNotice.message)
      return
    }

    try {
      const normalizedSearch = formatHotelSearchRequest(nextLocation, nextCheckInDate, nextCheckOutDate)
      setIsTourGroupTargetMode(false)
      setSearchLocation(normalizedSearch.location)
      setSearchCheckInDate(normalizedSearch.checkInDate)
      setSearchCheckOutDate(normalizedSearch.checkOutDate)
      setSelectedAdvertisement(null)
      setTargetHotelResponses([])

      const nextHotelPlannerResponses = await travelMvpApiClient.searchHotelsPlanner(normalizedSearch)

      setSearchNotice(
        nextHotelPlannerResponses.hotels.length > 0
          ? null
          : {
              kind: 'warning',
              message: translate('hotels.empty'),
            },
      )
      setHasSearchedHotels(true)
      setHotelPlannerResponses(nextHotelPlannerResponses.hotels)
      setDateWindowStart(addHotelDays(normalizedSearch.checkInDate, -3))
      setSearchRevision(revision => revision + 1)
    } catch (error) {
      setHasSearchedHotels(false)
      setHotelPlannerResponses([])
      setTargetHotelResponses([])
      const message = error instanceof Error ? error.message : translate('error.friendly.default')
      const notice = { kind: 'error', message } as const
      setSearchNotice(notice)
      onShowNotice('error', translate('error.friendly.default'), message)
    }
  }

  async function openAdvertisement(advertisementId: string) {
    const advertisement = deliveryAdvertisements.find(item => item.advertisementId === advertisementId) ?? null
    if (!advertisement) {
      return
    }

    setSelectedAdvertisement(advertisement)
    setIsTourGroupTargetMode(false)
    const nextHotelPlannerResponse = await travelMvpApiClient.getHotelDetailsPlanner(advertisement.targetResourceId, {
      checkInDate: searchCheckInDate,
      checkOutDate: searchCheckOutDate,
    })
    setSearchLocation(nextHotelPlannerResponse.location)
    setHasSearchedHotels(true)
    setTargetHotelResponses([])
    setHotelPlannerResponses([nextHotelPlannerResponse])
    setSearchNotice(null)
    setDateWindowStart(addHotelDays(searchCheckInDate, -3))
    setSearchRevision(revision => revision + 1)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function handleDateSelect(nextCheckInDate: string) {
    const nextCheckOutDate = addHotelDays(nextCheckInDate, 1)
    await executeHotelSearch(searchLocation, nextCheckInDate, nextCheckOutDate)
  }

  function onPreviousDateWindow() {
    setDateWindowStart(date => addHotelDays(date, -1))
  }

  function onNextDateWindow() {
    setDateWindowStart(date => addHotelDays(date, 1))
  }

  async function bookHotel(payload: HotelBookRequest) {
    if (!signedInUser) {
      setIsAuthDialogOpen(true)
      return
    }

    await runPageAction(async () => {
      await travelMvpApiClient.createHotelOrder({
        userId: signedInUser.userId,
        roomTypeId: payload.roomTypeId,
        guestTravelerIds: payload.guestTravelerIds,
        checkInDate: payload.checkInDate,
        checkOutDate: payload.checkOutDate,
        roomCount: payload.roomCount,
      })
      onNavigate('hotelOrders')
    }, translate('hotels.bookNow'), translate('notice.bookingCreated'))
  }

  async function loadReviewSummary(payload: { resourceType: string; resourceId: string }) {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return travelMvpApiClient.getReviewResourceSummary({
      userId: signedInUser.userId,
      resourceType: payload.resourceType,
      resourceId: payload.resourceId,
    })
  }

  async function loadReviewsByResource(payload: { resourceType: string; resourceId: string }) {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    const response = await travelMvpApiClient.listReviewsByResource({
      userId: signedInUser.userId,
      resourceType: payload.resourceType,
      resourceId: payload.resourceId,
    })
    return response.reviews
  }

  return {
    travelers,
    selectedTravelerIds,
    isBusy,
    isGuestMode: signedInUser === null,
    isTourGroupTargetMode,
    hotelResponses,
    targetHotelResponses,
    hasSearchedHotels,
    searchLocation,
    searchCheckInDate,
    searchCheckOutDate,
    roomCount,
    hotelPreference,
    nearbyPreference,
    searchNotice,
    featuredAdvertisement,
    selectedAdvertisement,
    hotelLowestNightlyPrice,
    dateWindowStart,
    isAuthDialogOpen,
    onPreviousDateWindow,
    onNextDateWindow,
    executeHotelSearch,
    openAdvertisement,
    handleDateSelect,
    setSearchLocation,
    setSearchCheckInDate,
    setSearchCheckOutDate,
    setHotelPreference,
    setNearbyPreference,
    toggleTravelerSelection,
    onRequireLogin: () => setIsAuthDialogOpen(true),
    onAuthDialogClose: () => setIsAuthDialogOpen(false),
    onAuthDialogConfirm: () => {
      setIsAuthDialogOpen(false)
      onNavigate('account')
    },
    loadReviewSummary,
    loadReviewsByResource,
    bookHotel,
    resultsSectionRef,
    noticeSectionRef,
  }
}
