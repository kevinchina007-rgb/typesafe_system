// 本文件定义 advertising 模块的 `AssignAdvertisementSlotPlanner`，负责广告投放位分配入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementSlotAssignmentRequest } from '@/microservices/advertising/objects/AdvertisementSlotAssignmentRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const assignAdvertisementSlot = (
  advertisementId: string,
  payload: AdvertisementSlotAssignmentRequest,
): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/AssignAdvertisementSlotPlanner', 'POST', { ...payload, advertisementId })
