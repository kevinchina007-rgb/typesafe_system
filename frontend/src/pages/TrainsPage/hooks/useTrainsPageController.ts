import { useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { formatTrainRecommendation, sortTrainResponses } from '../functions'
import type { TrainBookRequest, TrainSearchRequest, TrainSortMode, TrainsPageController, TrainsPageProps } from '../objects'
import { useTrainSearchState } from './useTrainSearchState'

export function useTrainsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: TrainsPageProps): TrainsPageController {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  const {
    trainResponses,
    hasSearchedTrains,
    searchDate,
    searchFromStation,
    searchToStation,
    setTrainResponses,
    setHasSearchedTrains,
    setSearchDate,
    setSearchFromStation,
    setSearchToStation,
  } = useTrainSearchState()
  const [trainSortMode, setTrainSortMode] = useState<TrainSortMode>('highSpeedPriority')
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)

  const sortedTrainResponses = useMemo(
    () => sortTrainResponses(trainResponses, searchFromStation, searchToStation, trainSortMode),
    [searchFromStation, searchToStation, trainResponses, trainSortMode],
  )
  const searchRecommendation = formatTrainRecommendation(searchFromStation, searchToStation, translate)

  async function executeTrainSearch() {
    const payload: TrainSearchRequest = {
      fromStation: searchFromStation,
      toStation: searchToStation,
      date: searchDate,
    }
    const trainListResponse = await travelMvpApiClient.listTrains(payload)
    setHasSearchedTrains(true)
    setTrainResponses(trainListResponse.trains)
  }

  async function bookTrain(payload: TrainBookRequest) {
    if (!signedInUser) {
      setIsAuthDialogOpen(true)
      return
    }

    await runPageAction(async () => {
      const createdOrder = await travelMvpApiClient.createOrder({
        ownerUserId: signedInUser.userId,
        orderCurrency: payload.orderCurrency,
      })
      await travelMvpApiClient.addTrainItemToOrder(createdOrder.orderId, {
        userId: signedInUser.userId,
        trainId: payload.trainId,
        travelerIds: payload.travelerIds,
        fromStationCode: payload.fromStationCode,
        toStationCode: payload.toStationCode,
        seatClass: payload.seatClass,
        seatPreference: payload.seatPreference,
      })
      onNavigate('trainOrders')
    }, translate('trains.bookNow'), translate('notice.bookingCreated'))
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
    isBusy,
    isGuestMode: signedInUser === null,
    trainResponses: sortedTrainResponses,
    hasSearchedTrains,
    searchRecommendation,
    searchDate,
    searchFromStation,
    searchToStation,
    trainSortMode,
    isAuthDialogOpen,
    executeTrainSearch,
    setSearchDate,
    setSearchFromStation,
    setSearchToStation,
    setTrainSortMode,
    onRequireLogin: () => setIsAuthDialogOpen(true),
    onAuthDialogClose: () => setIsAuthDialogOpen(false),
    onAuthDialogConfirm: () => {
      setIsAuthDialogOpen(false)
      onNavigate('account')
    },
    onBookTrain: bookTrain,
    loadReviewSummary,
    loadReviewsByResource,
  }
}
