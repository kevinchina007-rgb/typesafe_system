// 本文件定义 PauseAdvertisementDisplayPlanner，负责 advertising 模块的暂停编排和接口入口。

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { PauseAdvertisementDisplayRequest } from '@/microservices/advertising/objects/PauseAdvertisementDisplayRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const pauseAdvertisementDisplay = (advertisementId: string, payload: PauseAdvertisementDisplayRequest): Promise<AdvertisementResponse> =>
  executeJsonApiRequest('/PauseAdvertisementDisplayPlanner', 'POST', { ...payload, advertisementId })
