// 本文件定义 advertising 模块的 `GetAdvertisementDeliverySettingsPlanner`，负责投放设置读取入口。

import type { AdvertisementDeliverySettingsResponse } from '@/microservices/advertising/objects/AdvertisementDeliverySettingsResponse'
import type { GetAdvertisementDeliverySettingsRequest } from '@/microservices/advertising/objects/GetAdvertisementDeliverySettingsRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getAdvertisementDeliverySettings = (
  payload: GetAdvertisementDeliverySettingsRequest,
): Promise<AdvertisementDeliverySettingsResponse> =>
  executeJsonApiRequest('/GetAdvertisementDeliverySettingsPlanner', 'POST', payload)
