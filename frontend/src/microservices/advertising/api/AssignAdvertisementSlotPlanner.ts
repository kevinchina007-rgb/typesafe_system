import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

import type { AdvertisementSlotAssignmentRequest } from '@/microservices/advertising/objects/AdvertisementSlotAssignmentRequest'


import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const assignAdvertisementSlot = (advertisementId: string, payload: AdvertisementSlotAssignmentRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/AssignAdvertisementSlotPlanner', 'POST', { ...payload, advertisementId })
