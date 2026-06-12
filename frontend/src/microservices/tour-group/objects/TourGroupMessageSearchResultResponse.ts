// 本文件定义 tour-group 模块的 `TourGroupMessageSearchResultResponse`，作为响应数据并提供 JSON 编解码。

import type { TourGroupMessageResponse } from './TourGroupMessageResponse'

export type TourGroupMessageSearchResultResponse = {
  conversationId: string
  conversationTitle: string
  message: TourGroupMessageResponse
}
export const tourGroupMessageSearchResultResponseFromJson = (json: string): TourGroupMessageSearchResultResponse =>
  JSON.parse(json) as TourGroupMessageSearchResultResponse

export const tourGroupMessageSearchResultResponseToJson = (value: TourGroupMessageSearchResultResponse): string =>
  JSON.stringify(value)
