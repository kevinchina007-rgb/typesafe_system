// 本文件定义 advertising 模块的 `RejectAdvertisementPlanner`，负责广告审核驳回入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const rejectAdvertisement = (
  advertisementId: string,
  payload: AdvertisementReviewDecisionRequest,
): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/RejectAdvertisementPlanner', 'POST', { ...payload, advertisementId })
