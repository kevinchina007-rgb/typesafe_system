import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'



import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const approveAdvertisement = (advertisementId: string, payload: AdvertisementReviewDecisionRequest): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/approve`, 'POST', payload)
