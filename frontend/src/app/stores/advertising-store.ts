import { create } from 'zustand'

import { getManagerSnap } from '@/app/stores/manager-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AdvertisementImageUploadResponse } from '@/microservices/advertising/objects/AdvertisementImageUploadResponse'
import type { GenerateAdvertisementImageCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementImageCandidatesResponse'
import type { GenerateAdvertisementTextCandidatesResponse } from '@/microservices/advertising/objects/GenerateAdvertisementTextCandidatesResponse'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'
import type { AdvertisementSlotAssignmentRequest } from '@/microservices/advertising/objects/AdvertisementSlotAssignmentRequest'
import type { CreateAdvertisementRequest } from '@/microservices/advertising/objects/CreateAdvertisementRequest'
import type { UpdateAdvertisementRequest } from '@/microservices/advertising/objects/UpdateAdvertisementRequest'

type AdvertisingPlacementKey = 'flightBooking' | 'hotelBooking' | 'trainBooking' | 'attractionBooking'

type AdvertisingState = {
  ownerAdvertisements: AdvertisementResponse[]
  pendingReviewAdvertisements: AdvertisementResponse[]
  reviewedAdvertisements: AdvertisementResponse[]
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
  approveAdvertisement: (advertisementId: string, payload: AdvertisementReviewDecisionRequest) => Promise<AdvertisementResponse>
  rejectAdvertisement: (advertisementId: string, payload: AdvertisementReviewDecisionRequest) => Promise<AdvertisementResponse>
  assignAdvertisementSlot: (advertisementId: string, payload: AdvertisementSlotAssignmentRequest) => Promise<AdvertisementResponse>
}

type AdvertisingStore = AdvertisingState & AdvertisingActions

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

function sortAdvertisements(advertisements: AdvertisementResponse[]) {
  return [...advertisements].sort((left, right) => {
    if (left.priority !== right.priority) {
      return right.priority - left.priority
    }

    return Date.parse(right.updatedAt) - Date.parse(left.updatedAt)
  })
}

function upsertAdvertisement(advertisements: AdvertisementResponse[], nextAdvertisement: AdvertisementResponse) {
  const existingIndex = advertisements.findIndex(item => item.advertisementId === nextAdvertisement.advertisementId)
  if (existingIndex < 0) {
    return sortAdvertisements([nextAdvertisement, ...advertisements])
  }

  const nextAdvertisements = [...advertisements]
  nextAdvertisements[existingIndex] = nextAdvertisement
  return sortAdvertisements(nextAdvertisements)
}

function removeAdvertisement(advertisements: AdvertisementResponse[], advertisementId: string) {
  return advertisements.filter(item => item.advertisementId !== advertisementId)
}

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

function toPlacementValue(placement: AdvertisingPlacementKey) {
  if (placement === 'flightBooking') return 'FlightBookingPage'
  if (placement === 'trainBooking') return 'TrainBookingPage'
  return placement === 'hotelBooking' ? 'HotelBookingPage' : 'AttractionBookingPage'
}

function requireSignedInManager() {
  const manager = getManagerSnap().signedInManagerSession
  if (!manager) {
    throw new Error('manager_not_signed_in')
  }
  return manager
}

export const useAdvertisingStore = create<AdvertisingStore>()(set => ({
  ownerAdvertisements: [],
  pendingReviewAdvertisements: [],
  reviewedAdvertisements: [],
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
    const advertisements = sortAdvertisements(response.advertisements)
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
  generateAdvertisementImageCandidates: async payload => travelMvpApiClient.generateAdvertisementImageCandidates(payload),
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
    const advertisement = await travelMvpApiClient.approveAdvertisement(advertisementId, payload)
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
    const advertisement = await travelMvpApiClient.rejectAdvertisement(advertisementId, payload)
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
    const advertisement = await travelMvpApiClient.assignAdvertisementSlot(advertisementId, payload)
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
