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

export const getAdvertisementDeliverySettings = (placement: string): Promise<AdvertisementDeliverySettingsResponse> =>
  executeJsonApiRequest('/GetAdvertisementDeliverySettingsPlanner', 'POST', { placement })

export const saveAdvertisementDeliverySettings = (
  payload: SaveAdvertisementDeliverySettingsRequest,
): Promise<AdvertisementDeliverySettingsResponse> =>
  executeJsonApiRequest('/SaveAdvertisementDeliverySettingsPlanner', 'POST', payload)
