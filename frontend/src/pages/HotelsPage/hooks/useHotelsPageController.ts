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

// 规范化日期字符串，只保留年月日部分。
function normalizeDateOnly(value: string) {
  return value.trim().slice(0, 10)
}

// 酒店页面总控制器，负责搜索、广告、预订和评论加载。
export function useHotelsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: HotelsPageProps): HotelsPageController {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  // 搜索态由独立 hook 维护。
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
  // 已选出行人列表。
  const [selectedTravelerIds, setSelectedTravelerIds] = useState<string[]>([])
  // 当前是否处于团体定向预订模式。
  const [isTourGroupTargetMode, setIsTourGroupTargetMode] = useState(false)
  // 团体定向模式下的酒店结果。
  const [targetHotelResponses, setTargetHotelResponses] = useState<HotelPlannerResponse[]>([])

  // 当前可展示的广告列表。
  const deliveryAdvertisements = useDeliverableAdvertisements('hotelBooking')
  // 加载广告的方法。
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)
  // 当前选中的广告。
  const [selectedAdvertisement, setSelectedAdvertisement] = useState<AdvertisementResponse | null>(null)
  // 搜索提示信息。
  const [searchNotice, setSearchNotice] = useState<HotelSearchNotice | null>(null)
  // 搜索轮次，用于触发滚动定位。
  const [searchRevision, setSearchRevision] = useState(0)
  // 日期价格条起始日期。
  const [dateWindowStart, setDateWindowStart] = useState(() => addHotelDays(searchCheckInDate, -3))
  // 登录拦截弹窗是否打开。
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  // 结果区滚动锚点。
  const resultsSectionRef = useRef<HTMLDivElement | null>(null)
  // 提示区滚动锚点。
  const noticeSectionRef = useRef<HTMLDivElement | null>(null)

  // 广告位变化时刷新当前可展示的酒店广告。
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

  // 如果是旅游团定向预订，就先回填酒店搜索条件。
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
          roomTypes: nextHotelPlannerResponse.roomTypes.filter((roomType: (typeof nextHotelPlannerResponse.roomTypes)[number]) => roomType.roomTypeId === target.roomTypeId),
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

  // 入住日期变化时，重置日期条起点。
  useEffect(() => {
    setDateWindowStart(addHotelDays(searchCheckInDate, -3))
  }, [searchCheckInDate])

  // 搜索轮次变化后滚动到结果区。
  useEffect(() => {
    if (searchRevision <= 0) {
      return
    }

    resultsSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [searchRevision])

  // 搜索提示出现时滚动到提示区。
  useEffect(() => {
    if (!searchNotice) {
      return
    }

    noticeSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, [searchNotice])

  // 过滤掉当前不可用的出行人，并保证至少保留一组可选对象。
  useEffect(() => {
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  const featuredAdvertisement = deliveryAdvertisements[0] ?? null
  // 页面展示的最低房价。
  const hotelLowestNightlyPrice = useMemo(() => getLowestRoomPrice(hotelResponses), [hotelResponses])

  // 切换行人勾选状态。
  const toggleTravelerSelection = useCallback((travelerId: string) => {
    setSelectedTravelerIds(currentIds =>
      currentIds.includes(travelerId)
        ? currentIds.filter(nextTravelerId => nextTravelerId !== travelerId)
        : [...currentIds, travelerId],
    )
    }, [])

  // 执行一次酒店搜索并刷新页面状态。
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

  // 打开某个广告对应的酒店详情。
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

  // 选择某一天后重新发起搜索。
  async function handleDateSelect(nextCheckInDate: string) {
    const nextCheckOutDate = addHotelDays(nextCheckInDate, 1)
    await executeHotelSearch(searchLocation, nextCheckInDate, nextCheckOutDate)
  }

  // 日期条向前移动一天。
  function onPreviousDateWindow() {
    setDateWindowStart(date => addHotelDays(date, -1))
  }

  // 日期条向后移动一天。
  function onNextDateWindow() {
    setDateWindowStart(date => addHotelDays(date, 1))
  }

  // 提交酒店预订。
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

  // 加载资源下的评论列表。
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
