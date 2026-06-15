// 本文件定义 advertising 模块的 `GetAdvertisementDeliverySettingsRequest`，用于获取投放配置并提供 JSON 编解码。

export type GetAdvertisementDeliverySettingsRequest = {
  placement: string
}

export const getAdvertisementDeliverySettingsRequestFromJson = (json: string): GetAdvertisementDeliverySettingsRequest =>
  JSON.parse(json) as GetAdvertisementDeliverySettingsRequest

export const getAdvertisementDeliverySettingsRequestToJson = (value: GetAdvertisementDeliverySettingsRequest): string =>
  JSON.stringify(value)
