// 本文件定义 GetAdvertisementDeliverySettingsPlanner，负责 advertising 模块的获取编排和接口入口。

import type { AdvertisementDeliverySettingsResponse } from '@/microservices/advertising/objects/AdvertisementDeliverySettingsResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getAdvertisementDeliverySettings = (placement: string): Promise<AdvertisementDeliverySettingsResponse> =>
  executeJsonApiRequest('/GetAdvertisementDeliverySettingsPlanner', 'POST', { placement })
