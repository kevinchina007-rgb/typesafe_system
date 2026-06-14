import { useCallback, useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { addHotelDays } from '@/app/stores/models/hotel-booking-model'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { consumeTourGroupBookingTarget } from '@/pages/shared/tour-group-booking/tourGroupBookingTarget'
import { formatTrainRecommendation, normalizeTrainSearchRequestStations, sortTrainResponses } from '../functions'
import type { TrainBookRequest, TrainSortMode, TrainsPageController, TrainsPageProps } from '../objects'
import { useTrainSearchState } from './useTrainSearchState'
import type { TrainPlannerResponse } from '@/lib/mvp-types/index'

// 只截取日期字符串的年月日部分，供列车搜索使用。
function normalizeDateOnly(value: string) {
  return value.trim().slice(0, 10)
}

// TrainsPage 的页面控制器，负责搜索、预订、日期窗口和广告目标模式。
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
  const [isTourGroupTargetMode, setIsTourGroupTargetMode] = useState(false)
  const [targetTrainResponses, setTargetTrainResponses] = useState<TrainPlannerResponse[]>([])

  // 如果来自旅游团定向预订，就先回填列车搜索条件。
  useEffect(() => {
    const target = consumeTourGroupBookingTarget('trains')
    if (!target) {
      return
    }

    setIsTourGroupTargetMode(true)
    let cancelled = false

    void (async () => {
      try {
        const date = normalizeDateOnly(target.date)
        const train = await travelMvpApiClient.getTrain(target.trainId)
        if (cancelled) {
          return
        }

        const nextTrainResponse = {
          ...train,
          seatInventories: train.seatInventories.filter((seatInventory: (typeof train.seatInventories)[number]) => seatInventory.seatClass === target.seatClass || target.seatClass === null),
        }

        setSearchFromStation(target.fromStationCode)
        setSearchToStation(target.toStationCode)
        setSearchDate(date)
        setHasSearchedTrains(true)
        setTargetTrainResponses([nextTrainResponse])
        setTrainResponses([nextTrainResponse])
        setDateWindowStart(addHotelDays(date, -3))
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

  // 根据当前搜索条件计算排序后的列车结果。
  const sortedTrainResponses = useMemo(
    () => sortTrainResponses(trainResponses, searchFromStation, searchToStation, trainSortMode),
    [searchFromStation, searchToStation, trainResponses, trainSortMode],
  )
  // 计算当前搜索条件对应的推荐文案。
  const searchRecommendation = formatTrainRecommendation(searchFromStation, searchToStation, translate)

  // 过滤不可用的出行人，保持选择列表有效。
  useEffect(() => {
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  // 执行一次列车搜索并刷新结果。
  async function executeTrainSearch() {
    const normalizedStations = normalizeTrainSearchRequestStations(searchFromStation, searchToStation)
    setIsTourGroupTargetMode(false)
    setTargetTrainResponses([])
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

  // 日期条向前移动一天。
  function onPreviousDateWindow() {
    setDateWindowStart(date => addHotelDays(date, -1))
  }

  // 日期条向后移动一天。
  function onNextDateWindow() {
    setDateWindowStart(date => addHotelDays(date, 1))
  }

  // 选择某一天后重新查询列车。
  async function handleDateSelect(date: string) {
    setSearchDate(date)
    setIsTourGroupTargetMode(false)
    setTargetTrainResponses([])
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

  // 提交列车预订并创建订单。
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

  // 加载资源评论摘要。
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

  // 加载资源评论列表。
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
    isTourGroupTargetMode,
    targetTrainResponses,
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
