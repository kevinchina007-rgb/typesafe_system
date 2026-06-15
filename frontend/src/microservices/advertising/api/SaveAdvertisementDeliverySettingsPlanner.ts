// 本文件定义 advertising 模块的 `SaveAdvertisementDeliverySettingsPlanner`，负责投放设置保存入口。

import type { AdvertisementDeliverySettingsResponse } from '@/microservices/advertising/objects/AdvertisementDeliverySettingsResponse'
import type { SaveAdvertisementDeliverySettingsRequest } from '@/microservices/advertising/objects/SaveAdvertisementDeliverySettingsRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const saveAdvertisementDeliverySettings = (
  payload: SaveAdvertisementDeliverySettingsRequest,
): Promise<AdvertisementDeliverySettingsResponse> =>
  executeJsonApiRequest('/SaveAdvertisementDeliverySettingsPlanner', 'POST', payload)
