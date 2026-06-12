// 本文件定义 advertising 模块的 `AdvertisementOwnerActionRequest`，作为请求参数并提供 JSON 编解码。

export type AdvertisementOwnerActionRequest = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
}

export const advertisementOwnerActionRequestFromJson = (json: string): AdvertisementOwnerActionRequest =>
  JSON.parse(json) as AdvertisementOwnerActionRequest

export const advertisementOwnerActionRequestToJson = (value: AdvertisementOwnerActionRequest): string =>
  JSON.stringify(value)
