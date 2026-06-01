import { useCallback, useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { addHotelDays } from '@/app/stores/models/hotel-booking-model'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { formatTrainRecommendation, normalizeTrainSearchRequestStations, sortTrainResponses } from '../functions'
import type { TrainBookRequest, TrainSortMode, TrainsPageController, TrainsPageProps } from '../objects'
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
  const [selectedTravelerIds, setSelectedTravelerIds] = useState<string[]>([])
  const [trainSortMode, setTrainSortMode] = useState<TrainSortMode>('highSpeedPriority')
  const [dateWindowStart, setDateWindowStart] = useState(() => addHotelDays(searchDate, -3))
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)

  const sortedTrainResponses = useMemo(
    () => sortTrainResponses(trainResponses, searchFromStation, searchToStation, trainSortMode),
    [searchFromStation, searchToStation, trainResponses, trainSortMode],
  )
  const searchRecommendation = formatTrainRecommendation(searchFromStation, searchToStation, translate)

  useEffect(() => {
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  async function executeTrainSearch() {
    const normalizedStations = normalizeTrainSearchRequestStations(searchFromStation, searchToStation)
    const resolvedTrainResponses = (
      await travelMvpApiClient.listTrains({
        fromStation: normalizedStations.fromStation,
        toStation: normalizedStations.toStation,
        date: searchDate,
      })
    ).trains
    setHasSearchedTrains(true)
    setTrainResponses(resolvedTrainResponses)
    setDateWindowStart(addHotelDays(searchDate, -3))
  }

  function onPreviousDateWindow() {
    setDateWindowStart(date => addHotelDays(date, -1))
  }

  function onNextDateWindow() {
    setDateWindowStart(date => addHotelDays(date, 1))
  }

  async function handleDateSelect(date: string) {
    setSearchDate(date)
    const normalizedStations = normalizeTrainSearchRequestStations(searchFromStation, searchToStation)
    const resolvedTrainResponses = (
      await travelMvpApiClient.listTrains({
        fromStation: normalizedStations.fromStation,
        toStation: normalizedStations.toStation,
        date,
      })
    ).trains
    setHasSearchedTrains(true)
    setTrainResponses(resolvedTrainResponses)
    setDateWindowStart(addHotelDays(date, -3))
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

  const toggleTravelerSelection = useCallback((travelerId: string) => {
    setSelectedTravelerIds(currentIds =>
      currentIds.includes(travelerId)
        ? currentIds.filter(nextTravelerId => nextTravelerId !== travelerId)
        : [...currentIds, travelerId],
    )
  }, [])

  return {
    travelers,
    selectedTravelerIds,
    isBusy,
    isGuestMode: signedInUser === null,
    trainResponses: sortedTrainResponses,
    hasSearchedTrains,
    searchRecommendation,
    searchDate,
    searchFromStation,
    searchToStation,
    trainSortMode,
    dateWindowStart,
    isAuthDialogOpen,
    executeTrainSearch,
    onPreviousDateWindow,
    onNextDateWindow,
    handleDateSelect,
    setSearchDate,
    setSearchFromStation,
    setSearchToStation,
    setTrainSortMode,
    toggleTravelerSelection,
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
