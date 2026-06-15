// 本文件定义 advertising 模块的 `PauseAdvertisementPlanner`，负责广告所有者暂停入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AdvertisementOwnerActionRequest } from '@/microservices/advertising/objects/AdvertisementOwnerActionRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const pauseAdvertisement = (payload: AdvertisementOwnerActionRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/PauseAdvertisementPlanner', 'POST', payload)
