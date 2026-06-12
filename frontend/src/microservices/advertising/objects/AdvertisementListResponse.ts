// 本文件定义 advertising 模块的 `AdvertisementListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { AdvertisementResponse } from './AdvertisementResponse'

export type AdvertisementListResponse = {
  advertisements: AdvertisementResponse[]
}

export const advertisementListResponseFromJson = (json: string): AdvertisementListResponse =>
  JSON.parse(json) as AdvertisementListResponse

export const advertisementListResponseToJson = (value: AdvertisementListResponse): string =>
  JSON.stringify(value)
