// 本文件定义 RejectAdvertisementPlanner，负责 advertising 模块的驳回编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { RejectAdvertisementRequest } from '@/microservices/advertising/objects/RejectAdvertisementRequest'



import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const rejectAdvertisement = (advertisementId: string, payload: RejectAdvertisementRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/RejectAdvertisementPlanner', 'POST', { ...payload, advertisementId })
