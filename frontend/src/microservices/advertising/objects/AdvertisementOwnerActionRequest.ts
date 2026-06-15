// 本文件定义 advertising 模块的 `AdvertisementOwnerActionRequest`，用于广告所有者发起的提交/暂停等动作并提供 JSON 编解码。

export type AdvertisementOwnerActionRequest = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
}

export const advertisementOwnerActionRequestFromJson = (json: string): AdvertisementOwnerActionRequest =>
  JSON.parse(json) as AdvertisementOwnerActionRequest

export const advertisementOwnerActionRequestToJson = (value: AdvertisementOwnerActionRequest): string =>
  JSON.stringify(value)
