// 本文件定义 tour-group 模块的 `TourGroupMessageSearchResponse`，作为响应数据并提供 JSON 编解码。

import type { TourGroupMessageSearchResultResponse } from './TourGroupMessageSearchResultResponse'

export type TourGroupMessageSearchResponse = {
  results: TourGroupMessageSearchResultResponse[]
}
export const tourGroupMessageSearchResponseFromJson = (json: string): TourGroupMessageSearchResponse =>
  JSON.parse(json) as TourGroupMessageSearchResponse

export const tourGroupMessageSearchResponseToJson = (value: TourGroupMessageSearchResponse): string =>
  JSON.stringify(value)
