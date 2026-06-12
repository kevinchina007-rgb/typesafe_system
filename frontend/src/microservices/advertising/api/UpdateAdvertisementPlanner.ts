// 本文件定义 UpdateAdvertisementPlanner，负责 advertising 模块的更新编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'



import type { UpdateAdvertisementRequest } from '@/microservices/advertising/objects/UpdateAdvertisementRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const updateAdvertisement = (advertisementId: string, payload: UpdateAdvertisementRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/UpdateAdvertisementPlanner', 'POST', { ...payload, advertisementId })
