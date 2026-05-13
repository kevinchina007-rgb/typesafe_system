import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'




import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const pauseAdvertisement = (advertisementId: string): Promise<AdvertisementResponse> =>
    executeJsonApiRequest(`/advertisements/${advertisementId}/pause`, 'POST')
