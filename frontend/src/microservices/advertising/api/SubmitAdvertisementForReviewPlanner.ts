import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'




import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const submitAdvertisementForReview = (advertisementId: string): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/submit-review`, 'POST')
