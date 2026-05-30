import { useEffect, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AttractionsPageController, AttractionsPageProps } from '../objects'
import { mapAdvertisementAttractionSelection, loadAttractionReviews, loadAttractionReviewSummary, loadDetailedAttractions, splitAttractionHotSpotSelection } from '../functions'
import { useAttractionsSearchState } from './useAttractionsSearchState'

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
  const searchState = useAttractionsSearchState()
  const deliveryAdvertisements = useDeliverableAdvertisements('attractionBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)

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

  return {
    currentLanguage,
    isBusy,
    isGuestMode: signedInUser === null,
    travelers,
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
    isAuthDialogOpen,
    setAttractionResponses: searchState.setAttractionResponses,
    setHasSearchedAttractions: searchState.setHasSearchedAttractions,
    setSearchCity: searchState.setSearchCity,
    setKeyword: searchState.setKeyword,
    setUseDateDraft: searchState.setUseDateDraft,
    setTravelerCount: searchState.setTravelerCount,
    setAttractionType: searchState.setAttractionType,
    setSortPreference: searchState.setSortPreference,
    setSelectedQuickDatePreset: searchState.setSelectedQuickDatePreset,
    handleSearchAttractions: async () => {
      const nextAttractions = await loadDetailedAttractions({
        city: searchState.searchCity,
        keyword: searchState.keyword,
        useDate: searchState.useDateDraft,
        travelerCount: searchState.travelerCount,
        attractionType: searchState.attractionType,
        sortPreference: searchState.sortPreference,
      })
      searchState.setHasSearchedAttractions(true)
      searchState.setAttractionResponses(nextAttractions)
    },
    handleSelectHotAttraction: value => {
      const { city, keyword } = splitAttractionHotSpotSelection(value)
      searchState.setSearchCity(city)
      searchState.setKeyword(keyword)
    },
    handleOpenAdvertisement: async advertisement => {
      const nextAttraction = await travelMvpApiClient.getAttraction(advertisement.targetResourceId, {
        useDate: searchState.useDateDraft,
      })
      const { searchCity, keyword } = mapAdvertisementAttractionSelection(nextAttraction)
      searchState.setSearchCity(searchCity)
      searchState.setKeyword(keyword)
      searchState.setHasSearchedAttractions(true)
      searchState.setAttractionResponses([nextAttraction])
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    handleBookAttraction: async payload => {
      if (!signedInUser) {
        setIsAuthDialogOpen(true)
        return
      }
      await runPageAction(async () => {
        const createdOrder = await travelMvpApiClient.createOrder({
          ownerUserId: signedInUser.userId,
          orderCurrency: payload.orderCurrency,
        })
        await travelMvpApiClient.addAttractionItemToOrder(createdOrder.orderId, {
          attractionId: payload.attractionId,
          ticketTypeId: payload.ticketTypeId,
          sessionId: payload.sessionId,
          travelerIds: payload.travelerIds,
          useDate: payload.useDate,
        })
        onNavigate('bookings')
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
