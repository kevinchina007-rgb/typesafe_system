// 本文件定义 SubmitAdvertisementForReviewPlanner，负责 advertising 模块的提交编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { SubmitAdvertisementForReviewRequest } from '@/microservices/advertising/objects/SubmitAdvertisementForReviewRequest'




import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const submitAdvertisementForReview = (payload: SubmitAdvertisementForReviewRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/SubmitAdvertisementForReviewPlanner', 'POST', payload)
