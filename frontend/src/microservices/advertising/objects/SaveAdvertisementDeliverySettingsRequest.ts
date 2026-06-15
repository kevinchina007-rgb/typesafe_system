// 本文件定义 advertising 模块的 `SaveAdvertisementDeliverySettingsRequest`，用于保存投放配置并提供 JSON 编解码。

export type SaveAdvertisementDeliverySettingsRequest = {
  placement: string
  rotationIntervalSeconds: number
  playOrder: string
  startAt: string | null
  endAt: string | null
  updatedByManagerId: string
}

export const saveAdvertisementDeliverySettingsRequestFromJson = (json: string): SaveAdvertisementDeliverySettingsRequest =>
  JSON.parse(json) as SaveAdvertisementDeliverySettingsRequest

export const saveAdvertisementDeliverySettingsRequestToJson = (value: SaveAdvertisementDeliverySettingsRequest): string =>
  JSON.stringify(value)
