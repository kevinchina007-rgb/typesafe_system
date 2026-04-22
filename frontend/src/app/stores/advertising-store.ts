import { create } from 'zustand'

import { advertisingApiClient } from '../../lib/api-client/advertising'
import type {
  AdvertisementImageUploadResponse,
  AdvertisementResponse,
  AdvertisementReviewDecisionRequest,
  CreateAdvertisementRequest,
  UpdateAdvertisementRequest,
} from '../../lib/api-dtos/advertising'

type AdvertisingPlacementKey = 'hotelBooking' | 'attractionBooking'

type AdvertisingState = {
  ownerAdvertisements: AdvertisementResponse[]
  pendingReviewAdvertisements: AdvertisementResponse[]
  reviewedAdvertisements: AdvertisementResponse[]
  hotelBookingAdvertisements: AdvertisementResponse[]
  attractionBookingAdvertisements: AdvertisementResponse[]
  isLoading: boolean
}

type AdvertisingActions = {
  loadOwnerAdvertisements: () => Promise<AdvertisementResponse[]>
  loadPendingReviewAdvertisements: () => Promise<AdvertisementResponse[]>
  loadReviewedAdvertisements: () => Promise<AdvertisementResponse[]>
  loadDeliverableAdvertisements: (placement: AdvertisingPlacementKey) => Promise<AdvertisementResponse[]>
  createAdvertisement: (payload: CreateAdvertisementRequest) => Promise<AdvertisementResponse>
  uploadAdvertisementImage: (imageFile: File) => Promise<AdvertisementImageUploadResponse>
  updateAdvertisement: (advertisementId: string, payload: UpdateAdvertisementRequest) => Promise<AdvertisementResponse>
  submitAdvertisementForReview: (advertisementId: string) => Promise<AdvertisementResponse>
  pauseAdvertisement: (advertisementId: string) => Promise<AdvertisementResponse>
  approveAdvertisement: (advertisementId: string, payload: AdvertisementReviewDecisionRequest) => Promise<AdvertisementResponse>
  rejectAdvertisement: (advertisementId: string, payload: AdvertisementReviewDecisionRequest) => Promise<AdvertisementResponse>
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

function toPlacementValue(placement: AdvertisingPlacementKey) {
  return placement === 'hotelBooking' ? 'HotelBookingPage' : 'AttractionBookingPage'
}

export const useAdvertisingStore = create<AdvertisingStore>()(set => ({
  ownerAdvertisements: [],
  pendingReviewAdvertisements: [],
  reviewedAdvertisements: [],
  hotelBookingAdvertisements: [],
  attractionBookingAdvertisements: [],
  isLoading: false,
  loadOwnerAdvertisements: async () => {
    set({ isLoading: true })
    try {
      const response = await advertisingApiClient.listMyAdvertisements()
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
      const response = await advertisingApiClient.listPendingAdvertisements()
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
      const response = await advertisingApiClient.listReviewedAdvertisements()
      const advertisements = sortAdvertisements(response.advertisements)
      set({ reviewedAdvertisements: advertisements, isLoading: false })
      return advertisements
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  loadDeliverableAdvertisements: async placement => {
    const response = await advertisingApiClient.listDeliverableAdvertisements(toPlacementValue(placement))
    const advertisements = sortAdvertisements(response.advertisements)
    if (placement === 'hotelBooking') {
      set({ hotelBookingAdvertisements: advertisements })
    } else {
      set({ attractionBookingAdvertisements: advertisements })
    }
    return advertisements
  },
  createAdvertisement: async payload => {
    const advertisement = await advertisingApiClient.createAdvertisement(payload)
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
    }))
    return advertisement
  },
  uploadAdvertisementImage: async imageFile => advertisingApiClient.uploadAdvertisementImage(imageFile),
  updateAdvertisement: async (advertisementId, payload) => {
    const advertisement = await advertisingApiClient.updateAdvertisement(advertisementId, payload)
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      pendingReviewAdvertisements: upsertAdvertisement(state.pendingReviewAdvertisements, advertisement),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
    }))
    return advertisement
  },
  submitAdvertisementForReview: async advertisementId => {
    const advertisement = await advertisingApiClient.submitAdvertisementForReview(advertisementId)
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      pendingReviewAdvertisements: upsertAdvertisement(state.pendingReviewAdvertisements, advertisement),
      reviewedAdvertisements: removeAdvertisement(state.reviewedAdvertisements, advertisementId),
    }))
    return advertisement
  },
  pauseAdvertisement: async advertisementId => {
    const advertisement = await advertisingApiClient.pauseAdvertisement(advertisementId)
    set(state => ({
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      hotelBookingAdvertisements: removeAdvertisement(state.hotelBookingAdvertisements, advertisementId),
      attractionBookingAdvertisements: removeAdvertisement(state.attractionBookingAdvertisements, advertisementId),
    }))
    return advertisement
  },
  approveAdvertisement: async (advertisementId, payload) => {
    const advertisement = await advertisingApiClient.approveAdvertisement(advertisementId, payload)
    set(state => ({
      pendingReviewAdvertisements: removeAdvertisement(state.pendingReviewAdvertisements, advertisementId),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      hotelBookingAdvertisements:
        advertisement.placement === 'HotelBookingPage' && advertisementIsDeliverable(advertisement)
          ? upsertAdvertisement(state.hotelBookingAdvertisements, advertisement)
          : state.hotelBookingAdvertisements,
      attractionBookingAdvertisements:
        advertisement.placement === 'AttractionBookingPage' && advertisementIsDeliverable(advertisement)
          ? upsertAdvertisement(state.attractionBookingAdvertisements, advertisement)
          : state.attractionBookingAdvertisements,
    }))
    return advertisement
  },
  rejectAdvertisement: async (advertisementId, payload) => {
    const advertisement = await advertisingApiClient.rejectAdvertisement(advertisementId, payload)
    set(state => ({
      pendingReviewAdvertisements: removeAdvertisement(state.pendingReviewAdvertisements, advertisementId),
      reviewedAdvertisements: upsertAdvertisement(state.reviewedAdvertisements, advertisement),
      ownerAdvertisements: upsertAdvertisement(state.ownerAdvertisements, advertisement),
      hotelBookingAdvertisements: removeAdvertisement(state.hotelBookingAdvertisements, advertisementId),
      attractionBookingAdvertisements: removeAdvertisement(state.attractionBookingAdvertisements, advertisementId),
    }))
    return advertisement
  },
}))

export function useDeliverableAdvertisements(placement: AdvertisingPlacementKey) {
  return useAdvertisingStore(state =>
    placement === 'hotelBooking' ? state.hotelBookingAdvertisements : state.attractionBookingAdvertisements,
  )
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
