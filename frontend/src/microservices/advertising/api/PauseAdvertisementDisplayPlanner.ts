import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const pauseAdvertisementDisplay = (advertisementId: string, payload: AdvertisementReviewDecisionRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/PauseAdvertisementDisplayPlanner', 'POST', { ...payload, advertisementId })
