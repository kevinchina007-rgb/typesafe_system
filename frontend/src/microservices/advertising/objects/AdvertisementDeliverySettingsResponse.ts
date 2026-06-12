// 本文件定义 advertising 模块的 `AdvertisementDeliverySettingsResponse`，作为响应数据并提供 JSON 编解码。

export type AdvertisementDeliverySettingsResponse = {
  placement: string
  rotationIntervalSeconds: number
  playOrder: string
  startAt: string | null
  endAt: string | null
  updatedByManagerId: string | null
  updatedAt: string
}
