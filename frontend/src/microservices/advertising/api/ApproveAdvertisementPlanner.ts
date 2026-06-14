// 本文件定义 ApproveAdvertisementPlanner，负责 advertising 模块的审批通过编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { ApproveAdvertisementRequest } from '@/microservices/advertising/objects/ApproveAdvertisementRequest'



import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const approveAdvertisement = (advertisementId: string, payload: ApproveAdvertisementRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/ApproveAdvertisementPlanner', 'POST', { ...payload, advertisementId })
