// 本文件定义 advertising 模块的 `AdvertisementDeliverySettingsResponse`，用于投放设置响应并提供 JSON 编解码。

export type AdvertisementDeliverySettingsResponse = {
  placement: string
  rotationIntervalSeconds: number
  playOrder: string
  startAt: string | null
  endAt: string | null
  updatedByManagerId: string | null
  updatedAt: string
}

export const advertisementDeliverySettingsResponseFromJson = (json: string): AdvertisementDeliverySettingsResponse =>
  JSON.parse(json) as AdvertisementDeliverySettingsResponse

export const advertisementDeliverySettingsResponseToJson = (value: AdvertisementDeliverySettingsResponse): string =>
  JSON.stringify(value)
