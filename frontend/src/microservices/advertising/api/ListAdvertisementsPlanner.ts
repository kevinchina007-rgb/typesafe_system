import type { AdvertisementListResponse } from '@/microservices/advertising/objects/AdvertisementListResponse'





import { createQueryString, executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const listMyAdvertisements = (): Promise<AdvertisementListResponse> =>
    executeApiRequest('/advertisements/mine')

export const listPendingAdvertisements = (): Promise<AdvertisementListResponse> =>
    executeApiRequest('/advertisements/review/pending')

export const listReviewedAdvertisements = (): Promise<AdvertisementListResponse> =>
    executeApiRequest('/advertisements/review/history')

export const listDeliverableAdvertisements = (placement: string): Promise<AdvertisementListResponse> =>
    executeApiRequest(`/advertisements/delivery${createQueryString({ placement })}`)
