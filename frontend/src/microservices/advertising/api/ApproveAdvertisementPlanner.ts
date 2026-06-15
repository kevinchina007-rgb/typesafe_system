// 本文件定义 advertising 模块的 `ApproveAdvertisementPlanner`，负责广告审核通过入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const approveAdvertisement = (
  advertisementId: string,
  payload: AdvertisementReviewDecisionRequest,
): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/ApproveAdvertisementPlanner', 'POST', { ...payload, advertisementId })
