// 本文件定义 advertising 模块的 `SubmitAdvertisementForReviewPlanner`，负责广告提交审核入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementOwnerActionRequest } from '@/microservices/advertising/objects/AdvertisementOwnerActionRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const submitAdvertisementForReview = (
  payload: AdvertisementOwnerActionRequest,
): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/SubmitAdvertisementForReviewPlanner', 'POST', payload)
