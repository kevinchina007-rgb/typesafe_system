// 本文件定义 AssignAdvertisementSlotPlanner，负责 advertising 模块的分配编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

import type { AdvertisementSlotAssignmentRequest } from '@/microservices/advertising/objects/AdvertisementSlotAssignmentRequest'


import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const assignAdvertisementSlot = (advertisementId: string, payload: AdvertisementSlotAssignmentRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/AssignAdvertisementSlotPlanner', 'POST', { ...payload, advertisementId })
