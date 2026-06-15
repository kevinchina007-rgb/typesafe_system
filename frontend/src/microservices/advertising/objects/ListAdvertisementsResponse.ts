// 本文件定义 advertising 模块的 `ListAdvertisementsResponse`，用于列表响应数据并提供 JSON 编解码。

import type { AdvertisementResponse } from './AdvertisementResponse'

export type ListAdvertisementsResponse = {
  advertisements: AdvertisementResponse[]
}

export const listAdvertisementsResponseFromJson = (json: string): ListAdvertisementsResponse =>
  JSON.parse(json) as ListAdvertisementsResponse

export const listAdvertisementsResponseToJson = (value: ListAdvertisementsResponse): string =>
  JSON.stringify(value)
