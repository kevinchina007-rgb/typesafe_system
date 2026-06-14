// 本文件封装状态管理逻辑。

import { create } from 'zustand'

import { getManagerSnap } from '@/app/stores/manager-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AdvertisementImageUploadResponse } from '@/microservices/advertising/objects/AdvertisementImageUploadResponse'
import type { AdvertisementDeliverySettingsResponse } from '@/microservices/advertising/objects/AdvertisementDeliverySettingsResponse'
import type { GenerateAdvertisementImageCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementImageCandidatesResponse'
import type { GenerateAdvertisementTextCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementTextCandidatesResponse'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { ApproveAdvertisementRequest } from '@/microservices/advertising/objects/ApproveAdvertisementRequest'
import type { RejectAdvertisementRequest } from '@/microservices/advertising/objects/RejectAdvertisementRequest'
import type { AdvertisementSlotAssignmentRequest } from '@/microservices/advertising/objects/AdvertisementSlotAssignmentRequest'
import type { CreateAdvertisementRequest } from '@/microservices/advertising/objects/CreateAdvertisementRequest'
import type { UpdateAdvertisementRequest } from '@/microservices/advertising/objects/UpdateAdvertisementRequest'

type AdvertisingPlacementKey = 'flightBooking' | 'hotelBooking' | 'trainBooking' | 'attractionBooking'

type AdvertisingState = {
  ownerAdvertisements: AdvertisementResponse[]
  pendingReviewAdvertisements: AdvertisementResponse[]
  reviewedAdvertisements: AdvertisementResponse[]
  deliverySettingsByPlacement: Record<string, AdvertisementDeliverySettingsResponse>
  flightBookingAdvertisements: AdvertisementResponse[]
  hotelBookingAdvertisements: AdvertisementResponse[]
  trainBookingAdvertisements: AdvertisementResponse[]
  attractionBookingAdvertisements: AdvertisementResponse[]
  isLoading: boolean
}

type AdvertisingActions = {
  loadOwnerAdvertisements: () => Promise<AdvertisementResponse[]>
  loadPendingReviewAdvertisements: () => Promise<AdvertisementResponse[]>
  loadReviewedAdvertisements: () => Promise<AdvertisementResponse[]>
  loadDeliverableAdvertisements: (placement: AdvertisingPlacementKey) => Promise<AdvertisementResponse[]>
  createAdvertisement: (
    payload: Omit<CreateAdvertisementRequest, 'ownerManagerId' | 'ownerType' | 'ownerDisplayName'>
  ) => Promise<AdvertisementResponse>
  uploadAdvertisementImage: (imageFile: File) => Promise<AdvertisementImageUploadResponse>
  generateAdvertisementImageCandidates: (payload: {
    prompt: string
    supportingCopy?: string | null
    tone?: string | null
    resourceLabel?: string | null
    advertisementKind?: string | null
    imageFactoryKind?: string | null
    transparentBackground?: boolean | null
    width?: number | null
    height?: number | null
    candidateCount?: number | null
    avoidText?: string | null
  }) => Promise<GenerateAdvertisementImageCandidatesResponse>
  generateAdvertisementTextCandidates: (payload: {
    prompt: string
    sourceText?: string | null
    styleRequirement?: string | null
    focus?: string | null
    tone?: string | null
    resourceLabel?: string | null
    advertisementKind?: string | null
    candidateCount?: number | null
    avoidText?: string | null
  }) => Promise<GenerateAdvertisementTextCandidatesResponse>
  updateAdvertisement: (
    advertisementId: string,
    payload: Omit<UpdateAdvertisementRequest, 'ownerManagerId' | 'ownerType'>
  ) => Promise<AdvertisementResponse>
  submitAdvertisementForReview: (advertisementId: string) => Promise<AdvertisementResponse>
  pauseAdvertisement: (advertisementId: string) => Promise<AdvertisementResponse>
  approveAdvertisement: (advertisementId: string, payload: ApproveAdvertisementRequest) => Promise<AdvertisementResponse>
  rejectAdvertisement: (advertisementId: string, payload: RejectAdvertisementRequest) => Promise<AdvertisementResponse>
  assignAdvertisementSlot: (advertisementId: string, payload: AdvertisementSlotAssignmentRequest) => Promise<AdvertisementResponse>
  pauseAdvertisementDisplay: (advertisementId: string, reviewNote?: string | null) => Promise<AdvertisementResponse>
  loadAdvertisementDeliverySettings: (placement: string) => Promise<AdvertisementDeliverySettingsResponse>
  saveAdvertisementDeliverySettings: (payload: {
    placement: string
    rotationIntervalSeconds: number
    playOrder: string
    startAt: string | null
    endAt: string | null
  }) => Promise<AdvertisementDeliverySettingsResponse>
}

type AdvertisingStore = AdvertisingState & AdvertisingActions

// 判断广告当前是否满足展示条件。
function advertisementIsDeliverable(advertisement: AdvertisementResponse, currentTime = new Date()) {
  const startsAt = Date.parse(advertisement.startAt)
  const endsAt = Date.parse(advertisement.endAt)
  const now = currentTime.getTime()

  return (
    advertisement.reviewStatus === 'Approved' &&
    advertisement.deliveryStatus === 'Active' &&
    !Number.isNaN(startsAt) &&
    !Number.isNaN(endsAt) &&
    startsAt <= now &&
    endsAt >= now
  )
}

// 按优先级和更新时间排序广告。
function sortAdvertisements(advertisements: AdvertisementResponse[]) {
  return [...advertisements].sort((left, right) => {
    if (left.priority !== right.priority) {
      return right.priority - left.priority
    }

    return Date.parse(right.updatedAt) - Date.parse(left.updatedAt)
  })
}

// 按投放槽位和更新时间排序可展示广告。
function sortDeliverableAdvertisements(advertisements: AdvertisementResponse[]) {
  return [...advertisements].sort((left, right) => {
    const slotDelta = (left.slotIndex ?? 999) - (right.slotIndex ?? 999)
    if (slotDelta !== 0) return slotDelta
    return Date.parse(right.updatedAt) - Date.parse(left.updatedAt)
  })
}

// 新广告插入或覆盖已有同 ID 广告。
function upsertAdvertisement(advertisements: AdvertisementResponse[], nextAdvertisement: AdvertisementResponse) {
  const existingIndex = advertisements.findIndex(item => item.advertisementId === nextAdvertisement.advertisementId)
  if (existingIndex < 0) {
    return sortAdvertisements([nextAdvertisement, ...advertisements])
  }

  const nextAdvertisements = [...advertisements]
  nextAdvertisements[existingIndex] = nextAdvertisement
  return sortAdvertisements(nextAdvertisements)
}

// 从列表里移除指定广告。
function removeAdvertisement(advertisements: AdvertisementResponse[], advertisementId: string) {
  return advertisements.filter(item => item.advertisementId !== advertisementId)
}

// 在指定投放槽位里替换广告并保持排序。
function replaceAdvertisementSlot(
  advertisements: AdvertisementResponse[],
  nextAdvertisement: AdvertisementResponse,
) {
  return sortAdvertisements(
    advertisements
      .filter(item => !(item.placement === nextAdvertisement.placement && item.slotIndex === nextAdvertisement.slotIndex && item.advertisementId !== nextAdvertisement.advertisementId))
      .map(item => (item.advertisementId === nextAdvertisement.advertisementId ? nextAdvertisement : item)),
  )
}

// 把页面投放位映射成后端使用的字符串值。
function toPlacementValue(placement: AdvertisingPlacementKey) {
  if (placement === 'flightBooking') return 'FlightBookingPage'
  if (placement === 'trainBooking') return 'TrainBookingPage'
  return placement === 'hotelBooking' ? 'HotelBookingPage' : 'AttractionBookingPage'
}

// 读取当前登录管理者，未登录时直接抛错。
function requireSignedInManager() {
  const manager = getManagerSnap().signedInManagerSession
  if (!manager) {
    throw new Error('manager_not_signed_in')
  }
  return manager
}

// 广告状态仓库，负责广告列表、投放设置和投放位缓存。
export const useAdvertisingStore = create<AdvertisingStore>()(set => ({
  ownerAdvertisements: [],
  pendingReviewAdvertisements: [],
  reviewedAdvertisements: [],
  deliverySettingsByPlacement: {},
  flightBookingAdvertisements: [],
  hotelBookingAdvertisements: [],
  trainBookingAdvertisements: [],
  attractionBookingAdvertisements: [],
  isLoading: false,
  loadOwnerAdvertisements: async () => {
    set({ isLoading: true })
    try {
      const manager = requireSignedInManager()
      const response = await travelMvpApiClient.listMyAdvertisements(manager.managerId, manager.managerType)
      const advertisements = sortAdvertisements(response.advertisements)
      set({ ownerAdvertisements: advertisements, isLoading: false })
      return advertisements
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  loadPendingReviewAdvertisements: async () => {
    set({ isLoading: true })
    try {
      const response = await travelMvpApiClient.listPendingAdvertisements()
      const advertisements = sortAdvertisements(response.advertisements)
      set({ pendingReviewAdvertisements: advertisements, isLoading: false })
      return advertisements
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  loadReviewedAdvertisements: async () => {
    set({ isLoading: true })
    try {
      const response = await travelMvpApiClient.listReviewedAdvertisements()
      const advertisements = sortAdvertisements(response.advertisements)
      set({ reviewedAdvertisements: advertisements, isLoading: false })
      return advertisements
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  loadDeliverableAdvertisements: async placement => {
    const response = await travelMvpApiClient.listDeliverableAdvertisements(toPlacementValue(placement))
    const advertisements = sortDeliverableAdvertisements(response.advertisements)
    if (placement === 'flightBooking') {
      set({ flightBookingAdvertisements: advertisements })
    } else if (placement === 'hotelBooking') {
      set({ hotelBookingAdvertisements: advertisements })
    } else if (placement === 'trainBooking') {
      set({ trainBookingAdvertisements: advertisements })
    } else {
      set({ attractionBookingAdvertisements: advertisements })
    }
    return advertisements
  },
  createAdvertisement: async payload => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.createAdvertisement({
      ...payload,
      ownerManagerId: manager.managerId,
      ownerType: manager.managerType,
      ownerDisplayName: manager.displayName,
    })
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
    }))
    return advertisement
  },
  uploadAdvertisementImage: async imageFile => travelMvpApiClient.uploadAdvertisementImage(imageFile),
  // 生成广告图片候选。
  generateAdvertisementImageCandidates: async payload => travelMvpApiClient.generateAdvertisementImageCandidates(payload),
  // 生成广告文案候选。
  generateAdvertisementTextCandidates: async payload => travelMvpApiClient.generateAdvertisementTextCandidates(payload),
  updateAdvertisement: async (advertisementId, payload) => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.updateAdvertisement(advertisementId, {
      ...payload,
      ownerManagerId: manager.managerId,
      ownerType: manager.managerType,
    })
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      pendingReviewAdvertisements: upsertAdvertisement(state.pendingReviewAdvertisements, advertisement),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
    }))
    return advertisement
  },
  submitAdvertisementForReview: async advertisementId => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.submitAdvertisementForReview({
      advertisementId,
      ownerManagerId: manager.managerId,
      ownerType: manager.managerType,
    })
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      pendingReviewAdvertisements: upsertAdvertisement(state.pendingReviewAdvertisements, advertisement),
      reviewedAdvertisements: removeAdvertisement(state.reviewedAdvertisements, advertisementId),
    }))
    return advertisement
  },
  pauseAdvertisement: async advertisementId => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.pauseAdvertisement({
      advertisementId,
      ownerManagerId: manager.managerId,
      ownerType: manager.managerType,
    })
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      flightBookingAdvertisements: removeAdvertisement(state.flightBookingAdvertisements, advertisementId),
      hotelBookingAdvertisements: removeAdvertisement(state.hotelBookingAdvertisements, advertisementId),
      trainBookingAdvertisements: removeAdvertisement(state.trainBookingAdvertisements, advertisementId),
      attractionBookingAdvertisements: removeAdvertisement(state.attractionBookingAdvertisements, advertisementId),
    }))
    return advertisement
  },
  approveAdvertisement: async (advertisementId, payload) => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.approveAdvertisement(advertisementId, {
      ...payload,
      reviewerManagerId: manager.managerId,
    })
    set(state => ({
      pendingReviewAdvertisements: removeAdvertisement(state.pendingReviewAdvertisements, advertisementId),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      flightBookingAdvertisements:
        advertisement.placement === 'FlightBookingPage' && advertisementIsDeliverable(advertisement)
          ? upsertAdvertisement(state.flightBookingAdvertisements, advertisement)
          : state.flightBookingAdvertisements,
      hotelBookingAdvertisements:
        advertisement.placement === 'HotelBookingPage' && advertisementIsDeliverable(advertisement)
          ? upsertAdvertisement(state.hotelBookingAdvertisements, advertisement)
          : state.hotelBookingAdvertisements,
      trainBookingAdvertisements:
        advertisement.placement === 'TrainBookingPage' && advertisementIsDeliverable(advertisement)
          ? upsertAdvertisement(state.trainBookingAdvertisements, advertisement)
          : state.trainBookingAdvertisements,
      attractionBookingAdvertisements:
        advertisement.placement === 'AttractionBookingPage' && advertisementIsDeliverable(advertisement)
          ? upsertAdvertisement(state.attractionBookingAdvertisements, advertisement)
          : state.attractionBookingAdvertisements,
    }))
    return advertisement
  },
  rejectAdvertisement: async (advertisementId, payload) => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.rejectAdvertisement(advertisementId, {
      ...payload,
      reviewerManagerId: manager.managerId,
    })
    set(state => ({
      pendingReviewAdvertisements: removeAdvertisement(state.pendingReviewAdvertisements, advertisementId),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      flightBookingAdvertisements: removeAdvertisement(state.flightBookingAdvertisements, advertisementId),
      hotelBookingAdvertisements: removeAdvertisement(state.hotelBookingAdvertisements, advertisementId),
      trainBookingAdvertisements: removeAdvertisement(state.trainBookingAdvertisements, advertisementId),
      attractionBookingAdvertisements: removeAdvertisement(state.attractionBookingAdvertisements, advertisementId),
    }))
    return advertisement
  },
  assignAdvertisementSlot: async (advertisementId, payload) => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.assignAdvertisementSlot(advertisementId, {
      ...payload,
      reviewerManagerId: manager.managerId,
    })
    set(state => {
      const deliverableTargetKey =
        advertisement.placement === 'FlightBookingPage'
          ? 'flightBookingAdvertisements'
          : advertisement.placement === 'HotelBookingPage'
            ? 'hotelBookingAdvertisements'
            : advertisement.placement === 'TrainBookingPage'
              ? 'trainBookingAdvertisements'
              : 'attractionBookingAdvertisements'
      const nextDeliverables = replaceAdvertisementSlot(
        state[deliverableTargetKey],
        advertisement,
      )

      return {
        ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
        pendingReviewAdvertisements: removeAdvertisement(state.pendingReviewAdvertisements, advertisementId),
        reviewedAdvertisements: replaceAdvertisementSlot(upsertAdvertisement(state.reviewedAdvertisements, advertisement), advertisement),
        flightBookingAdvertisements:
          deliverableTargetKey === 'flightBookingAdvertisements' ? nextDeliverables : state.flightBookingAdvertisements,
        hotelBookingAdvertisements:
          deliverableTargetKey === 'hotelBookingAdvertisements' ? nextDeliverables : state.hotelBookingAdvertisements,
        trainBookingAdvertisements:
          deliverableTargetKey === 'trainBookingAdvertisements' ? nextDeliverables : state.trainBookingAdvertisements,
        attractionBookingAdvertisements:
          deliverableTargetKey === 'attractionBookingAdvertisements' ? nextDeliverables : state.attractionBookingAdvertisements,
      }
    })
    return advertisement
  },
  pauseAdvertisementDisplay: async (advertisementId, reviewNote) => {
    const manager = requireSignedInManager()
    const advertisement = await travelMvpApiClient.pauseAdvertisementDisplay(advertisementId, {
      reviewerManagerId: manager.managerId,
      reviewNote,
    })
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      flightBookingAdvertisements: removeAdvertisement(state.flightBookingAdvertisements, advertisementId),
      hotelBookingAdvertisements: removeAdvertisement(state.hotelBookingAdvertisements, advertisementId),
      trainBookingAdvertisements: removeAdvertisement(state.trainBookingAdvertisements, advertisementId),
      attractionBookingAdvertisements: removeAdvertisement(state.attractionBookingAdvertisements, advertisementId),
    }))
    return advertisement
  },
  loadAdvertisementDeliverySettings: async placement => {
    const settings = await travelMvpApiClient.getAdvertisementDeliverySettings(placement)
    set(state => ({ deliverySettingsByPlacement: { ...state.deliverySettingsByPlacement, [placement]: settings } }))
    return settings
  },
  saveAdvertisementDeliverySettings: async payload => {
    const manager = requireSignedInManager()
    const settings = await travelMvpApiClient.saveAdvertisementDeliverySettings({
      ...payload,
      updatedByManagerId: manager.managerId,
    })
    set(state => ({ deliverySettingsByPlacement: { ...state.deliverySettingsByPlacement, [settings.placement]: settings } }))
    return settings
  },
}))

export function useDeliverableAdvertisements(placement: AdvertisingPlacementKey) {
  return useAdvertisingStore(state => {
    if (placement === 'flightBooking') return state.flightBookingAdvertisements
    if (placement === 'hotelBooking') return state.hotelBookingAdvertisements
    if (placement === 'trainBooking') return state.trainBookingAdvertisements
    return state.attractionBookingAdvertisements
  })
}

export function getAdvertisingSnap() {
  return useAdvertisingStore.getState()
}

export function loadOwnerAdvertisements() {
  return useAdvertisingStore.getState().loadOwnerAdvertisements()
}

export function loadPendingReviewAdvertisements() {
  return useAdvertisingStore.getState().loadPendingReviewAdvertisements()
}

export function loadReviewedAdvertisements() {
  return useAdvertisingStore.getState().loadReviewedAdvertisements()
}

export function loadDeliverableAdvertisements(placement: AdvertisingPlacementKey) {
  return useAdvertisingStore.getState().loadDeliverableAdvertisements(placement)
}
