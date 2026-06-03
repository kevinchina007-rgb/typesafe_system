import { useCallback, useEffect, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { addHotelDays } from '@/app/stores/models/hotel-booking-model'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { consumeTourGroupBookingTarget } from '@/pages/shared/tour-group-booking/tourGroupBookingTarget'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AttractionResponse } from '@/lib/mvp-types/index'
import type { AttractionsPageController, AttractionsPageProps } from '../objects'
import { mapAdvertisementAttractionSelection, loadAttractionReviews, loadAttractionReviewSummary, loadDetailedAttractions, splitAttractionHotSpotSelection } from '../functions'
import { summarizeAttractionEligibilityFailure } from '@/app/stores/models/attraction-booking-model'
import { useAttractionsSearchState } from './useAttractionsSearchState'

function normalizeDateOnly(value: string) {
  return value.trim().slice(0, 10)
}

export function useAttractionsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: AttractionsPageProps): AttractionsPageController {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  const [selectedTravelerIds, setSelectedTravelerIds] = useState<string[]>([])
  const [focusedSessionId, setFocusedSessionId] = useState<string | null>(null)
  const [isTourGroupTargetMode, setIsTourGroupTargetMode] = useState(false)
  const [targetAttractionResponses, setTargetAttractionResponses] = useState<AttractionResponse[]>([])
  const searchState = useAttractionsSearchState()
  const [dateWindowStart, setDateWindowStart] = useState(() => addHotelDays(searchState.useDateDraft, -3))
  const deliveryAdvertisements = useDeliverableAdvertisements('attractionBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)

  useEffect(() => {
    const target = consumeTourGroupBookingTarget('attractions')
    if (!target) {
      return
    }

    let cancelled = false

    void (async () => {
      try {
        const useDate = normalizeDateOnly(target.useDate)
        const nextAttraction = await travelMvpApiClient.getAttraction(target.attractionId, {
          useDate,
        })
        if (cancelled) {
          return
        }

        setIsTourGroupTargetMode(true)
        const { searchCity, keyword } = mapAdvertisementAttractionSelection(nextAttraction)
        searchState.setSearchCity(searchCity)
        searchState.setKeyword(keyword)
        searchState.setUseDateDraft(useDate)
        searchState.setHasSearchedAttractions(true)
        const nextTargetAttractionResponses = [
          {
            ...nextAttraction,
            ticketTypes: nextAttraction.ticketTypes.filter(ticketType => ticketType.ticketTypeId === target.ticketTypeId),
          },
        ]
        setTargetAttractionResponses(nextTargetAttractionResponses)
        searchState.setAttractionResponses(nextTargetAttractionResponses)
        setFocusedSessionId(target.sessionId)
        setDateWindowStart(addHotelDays(useDate, -3))
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
  }, [onShowNotice, searchState, translate])

  useEffect(() => {
    void loadDeliverableAdvertisements('attractionBooking')
    const reloadDeliverableAdvertisements = () => {
      void loadDeliverableAdvertisements('attractionBooking')
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
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  const toggleTravelerSelection = useCallback((travelerId: string) => {
    setSelectedTravelerIds(currentIds =>
      currentIds.includes(travelerId)
        ? currentIds.filter(nextTravelerId => nextTravelerId !== travelerId)
        : [...currentIds, travelerId],
    )
  }, [])

  return {
    currentLanguage,
    isBusy,
    isGuestMode: signedInUser === null,
    isTourGroupTargetMode,
    travelers,
    selectedTravelerIds,
    focusedSessionId,
    deliveryAdvertisements,
    attractionResponses: searchState.attractionResponses,
    hasSearchedAttractions: searchState.hasSearchedAttractions,
    searchCity: searchState.searchCity,
    keyword: searchState.keyword,
    useDateDraft: searchState.useDateDraft,
    travelerCount: searchState.travelerCount,
    attractionType: searchState.attractionType,
    sortPreference: searchState.sortPreference,
    selectedQuickDatePreset: searchState.selectedQuickDatePreset,
    dateWindowStart,
    isAuthDialogOpen,
    targetAttractionResponses,
    setAttractionResponses: searchState.setAttractionResponses,
    setHasSearchedAttractions: searchState.setHasSearchedAttractions,
    setSearchCity: searchState.setSearchCity,
    setKeyword: searchState.setKeyword,
    setUseDateDraft: searchState.setUseDateDraft,
    setTravelerCount: searchState.setTravelerCount,
    setAttractionType: searchState.setAttractionType,
    setSortPreference: searchState.setSortPreference,
    setSelectedQuickDatePreset: searchState.setSelectedQuickDatePreset,
    toggleTravelerSelection,
    handleSearchAttractions: async () => {
      const nextAttractions = await loadDetailedAttractions({
        city: searchState.searchCity,
        keyword: searchState.keyword,
        useDate: searchState.useDateDraft,
        sortPreference: searchState.sortPreference,
      })
      setIsTourGroupTargetMode(false)
      setTargetAttractionResponses([])
      searchState.setHasSearchedAttractions(true)
      setFocusedSessionId(null)
      searchState.setAttractionResponses(nextAttractions)
      setDateWindowStart(addHotelDays(searchState.useDateDraft, -3))
    },
    onPreviousDateWindow: () => {
      setDateWindowStart(date => addHotelDays(date, -1))
    },
    onNextDateWindow: () => {
      setDateWindowStart(date => addHotelDays(date, 1))
    },
    handleDateSelect: async date => {
      searchState.setUseDateDraft(date)
      const nextAttractions = await loadDetailedAttractions({
        city: searchState.searchCity,
        keyword: searchState.keyword,
        useDate: date,
        sortPreference: searchState.sortPreference,
      })
      setIsTourGroupTargetMode(false)
      setTargetAttractionResponses([])
      searchState.setHasSearchedAttractions(true)
      setFocusedSessionId(null)
      searchState.setAttractionResponses(nextAttractions)
      setDateWindowStart(addHotelDays(date, -3))
    },
    handleSelectHotAttraction: value => {
      const { city, keyword } = splitAttractionHotSpotSelection(value)
      setIsTourGroupTargetMode(false)
      searchState.setSearchCity(city)
      searchState.setKeyword(keyword)
    },
    handleOpenAdvertisement: async advertisement => {
      const nextAttraction = await travelMvpApiClient.getAttraction(advertisement.targetResourceId, {
        useDate: searchState.useDateDraft,
      })
      const { searchCity, keyword } = mapAdvertisementAttractionSelection(nextAttraction)
      setIsTourGroupTargetMode(false)
      setTargetAttractionResponses([])
      searchState.setSearchCity(searchCity)
      searchState.setKeyword(keyword)
      searchState.setHasSearchedAttractions(true)
      setFocusedSessionId(null)
      searchState.setAttractionResponses([nextAttraction])
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    handleBookAttraction: async payload => {
      if (!signedInUser) {
        setIsAuthDialogOpen(true)
        return
      }
      await runPageAction(async () => {
        const travelerSelectionError = summarizeAttractionEligibilityFailure(travelers, payload.travelerIds, payload.rules, payload.useDate)
        if (travelerSelectionError) {
          throw new Error(travelerSelectionError)
        }
        const createdOrder = await travelMvpApiClient.createOrder({
          ownerUserId: signedInUser.userId,
          orderCurrency: payload.orderCurrency,
        })
        await travelMvpApiClient.addAttractionItemToOrder(createdOrder.orderId, {
          userId: signedInUser.userId,
          attractionId: payload.attractionId,
          ticketTypeId: payload.ticketTypeId,
          sessionId: payload.sessionId,
          travelerIds: payload.travelerIds,
          useDate: payload.useDate,
        })
        onNavigate('attractionOrders')
      }, translate('attractions.bookNow'), translate('notice.bookingCreated'))
    },
    handleLoadReviewSummary: payload => loadAttractionReviewSummary(signedInUser, translate, payload),
    handleLoadReviews: payload => loadAttractionReviews(signedInUser, translate, payload),
    handleSelectQuickDatePreset: (preset, nextDate) => {
      searchState.setSelectedQuickDatePreset(preset)
      searchState.setUseDateDraft(nextDate)
    },
    handleOpenAuthDialog: () => setIsAuthDialogOpen(true),
    handleCloseAuthDialog: () => setIsAuthDialogOpen(false),
    handleConfirmAuthDialog: () => {
      setIsAuthDialogOpen(false)
      onNavigate('account')
    },
  }
}
