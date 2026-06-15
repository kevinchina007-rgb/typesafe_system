// 本文件定义 advertising 模块的 `PauseAdvertisementDisplayPlanner`，负责广告下线展示入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const pauseAdvertisementDisplay = (
  advertisementId: string,
  payload: AdvertisementReviewDecisionRequest,
): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/PauseAdvertisementDisplayPlanner', 'POST', { ...payload, advertisementId })
