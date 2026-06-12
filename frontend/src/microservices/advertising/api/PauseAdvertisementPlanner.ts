// 本文件定义 PauseAdvertisementPlanner，负责 advertising 模块的暂停编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementOwnerActionRequest } from '@/microservices/advertising/objects/AdvertisementOwnerActionRequest'




import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const pauseAdvertisement = (payload: AdvertisementOwnerActionRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/PauseAdvertisementPlanner', 'POST', payload)
