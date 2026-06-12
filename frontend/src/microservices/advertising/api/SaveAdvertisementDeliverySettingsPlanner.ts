// 本文件定义 SaveAdvertisementDeliverySettingsPlanner，负责 advertising 模块的保存编排和接口入口。

import type { AdvertisementDeliverySettingsResponse } from '@/microservices/advertising/objects/AdvertisementDeliverySettingsResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export type SaveAdvertisementDeliverySettingsRequest = {
  placement: string
  rotationIntervalSeconds: number
  playOrder: string
  startAt: string | null
  endAt: string | null
  updatedByManagerId: string
}

export const saveAdvertisementDeliverySettings = (
  payload: SaveAdvertisementDeliverySettingsRequest,
): Promise<AdvertisementDeliverySettingsResponse> =>
  executeJsonApiRequest('/SaveAdvertisementDeliverySettingsPlanner', 'POST', payload)
