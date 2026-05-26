import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementOwnerActionRequest } from '@/microservices/advertising/objects/AdvertisementOwnerActionRequest'




import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const submitAdvertisementForReview = (payload: AdvertisementOwnerActionRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/SubmitAdvertisementForReviewPlanner', 'POST', payload)
