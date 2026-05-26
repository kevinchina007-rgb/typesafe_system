import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'



import type { UpdateAdvertisementRequest } from '@/microservices/advertising/objects/UpdateAdvertisementRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const updateAdvertisement = (advertisementId: string, payload: UpdateAdvertisementRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/UpdateAdvertisementPlanner', 'POST', { ...payload, advertisementId })
