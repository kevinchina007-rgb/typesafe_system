import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'


import type { CreateAdvertisementRequest } from '@/microservices/advertising/objects/CreateAdvertisementRequest'

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createAdvertisement = (payload: CreateAdvertisementRequest): Promise<AdvertisementResponse> =>
    executeJsonApiRequest('/advertisements', 'POST', payload)
