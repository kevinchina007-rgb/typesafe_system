// 本文件定义 RejectAdvertisementPlanner，负责 advertising 模块的驳回编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementReviewDecisionRequest } from '@/microservices/advertising/objects/AdvertisementReviewDecisionRequest'



import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const rejectAdvertisement = (advertisementId: string, payload: AdvertisementReviewDecisionRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/RejectAdvertisementPlanner', 'POST', { ...payload, advertisementId })
