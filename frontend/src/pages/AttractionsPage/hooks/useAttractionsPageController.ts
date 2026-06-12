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

// 只截取日期字符串的年月日部分，供景点搜索日期使用。
function normalizeDateOnly(value: string) {
  return value.trim().slice(0, 10)
}

// AttractionsPage 的页面控制器，负责搜索、预订、广告和日期窗口状态。
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

  // 如果是旅游团定向预订，就先回填景点搜索条件。
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
            ticketTypes: nextAttraction.ticketTypes.filter((ticketType: (typeof nextAttraction.ticketTypes)[number]) => ticketType.ticketTypeId === target.ticketTypeId),
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

  // 广告位内容变化时刷新当前可展示广告。
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

  // 过滤掉当前不可用的出行人，并保证至少保留一组可选对象。
  useEffect(() => {
    const availableTravelerIds = travelers.map(traveler => traveler.travelerId)
    setSelectedTravelerIds(currentIds => {
      const nextIds = currentIds.filter(travelerId => availableTravelerIds.includes(travelerId))
      return nextIds.length > 0 ? nextIds : availableTravelerIds
    })
  }, [travelers])

  // 切换出行人勾选状态。
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
    // 拉取景点列表并刷新当前搜索结果。
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
    // 向前切换日期窗口。
    onPreviousDateWindow: () => {
      setDateWindowStart(date => addHotelDays(date, -1))
    },
    // 向后切换日期窗口。
    onNextDateWindow: () => {
      setDateWindowStart(date => addHotelDays(date, 1))
    },
    // 选择某一天后重新查询景点。
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
    // 从热搜词里拆出城市和关键词并回填搜索条件。
    handleSelectHotAttraction: value => {
      const { city, keyword } = splitAttractionHotSpotSelection(value)
      setIsTourGroupTargetMode(false)
      searchState.setSearchCity(city)
      searchState.setKeyword(keyword)
    },
    // 打开广告项对应的景点详情并同步搜索条件。
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
    // 提交景点预订并创建订单。
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
    // 加载评价摘要。
    handleLoadReviewSummary: payload => loadAttractionReviewSummary(signedInUser, translate, payload),
    // 加载评价列表。
    handleLoadReviews: payload => loadAttractionReviews(signedInUser, translate, payload),
    // 处理快捷日期预设。
    handleSelectQuickDatePreset: (preset, nextDate) => {
      searchState.setSelectedQuickDatePreset(preset)
      searchState.setUseDateDraft(nextDate)
    },
    // 打开登录引导弹窗。
    handleOpenAuthDialog: () => setIsAuthDialogOpen(true),
    // 关闭登录引导弹窗。
    handleCloseAuthDialog: () => setIsAuthDialogOpen(false),
    // 确认登录引导并跳转到账号页。
    handleConfirmAuthDialog: () => {
      setIsAuthDialogOpen(false)
      onNavigate('account')
    },
  }
}
