// 本文件定义 SubmitAdvertisementForReviewPlanner，负责 advertising 模块的提交编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementOwnerActionRequest } from '@/microservices/advertising/objects/AdvertisementOwnerActionRequest'




import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const submitAdvertisementForReview = (payload: AdvertisementOwnerActionRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/SubmitAdvertisementForReviewPlanner', 'POST', payload)
