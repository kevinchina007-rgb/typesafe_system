// 本文件定义 CreateAdvertisementPlanner，负责 advertising 模块的创建编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'


import type { CreateAdvertisementRequest } from '@/microservices/advertising/objects/CreateAdvertisementRequest'

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createAdvertisement = (payload: CreateAdvertisementRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/CreateAdvertisementPlanner', 'POST', payload)
