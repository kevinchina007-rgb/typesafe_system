import type { AdvertisementListResponse } from '@/microservices/advertising/objects/AdvertisementListResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listMyAdvertisements = (ownerManagerId: string, ownerType: string): Promise<AdvertisementListResponse> =>
  executeJsonApiRequest('/ListAdvertisementsPlanner', 'POST', {
    placement: undefined,
    reviewStatus: undefined,
    reviewStatuses: undefined,
    ownerManagerId,
    ownerType,
    deliverableOnly: undefined,
    currentTime: undefined,
  })

export const listPendingAdvertisements = (): Promise<AdvertisementListResponse> =>
  executeJsonApiRequest('/ListAdvertisementsPlanner', 'POST', {
    placement: undefined,
    reviewStatus: 'Pending',
    reviewStatuses: undefined,
    ownerManagerId: undefined,
    ownerType: undefined,
    deliverableOnly: undefined,
    currentTime: undefined,
  })

export const listReviewedAdvertisements = (): Promise<AdvertisementListResponse> =>
  executeJsonApiRequest('/ListAdvertisementsPlanner', 'POST', {
    placement: undefined,
    reviewStatus: undefined,
    reviewStatuses: ['Approved', 'Rejected'],
    ownerManagerId: undefined,
    ownerType: undefined,
    deliverableOnly: undefined,
    currentTime: undefined,
  })

export const listDeliverableAdvertisements = (placement: string): Promise<AdvertisementListResponse> =>
  executeJsonApiRequest('/ListAdvertisementsPlanner', 'POST', {
    placement,
    reviewStatus: 'Approved',
    reviewStatuses: undefined,
    ownerManagerId: undefined,
    ownerType: undefined,
    deliverableOnly: true,
    currentTime: undefined,
  })
