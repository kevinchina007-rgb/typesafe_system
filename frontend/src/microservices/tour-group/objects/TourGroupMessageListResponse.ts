// 本文件定义 tour-group 模块的 `TourGroupMessageListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { TourGroupMessageResponse } from './TourGroupMessageResponse'

export type TourGroupMessageListResponse = {
  messages: TourGroupMessageResponse[]
}
export const tourGroupMessageListResponseFromJson = (json: string): TourGroupMessageListResponse =>
  JSON.parse(json) as TourGroupMessageListResponse

export const tourGroupMessageListResponseToJson = (value: TourGroupMessageListResponse): string =>
  JSON.stringify(value)
