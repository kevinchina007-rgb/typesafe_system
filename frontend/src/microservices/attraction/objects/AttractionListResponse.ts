// 本文件定义 attraction 模块的 `AttractionListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { AttractionResponse } from './AttractionResponse'

export type AttractionListResponse = {
  attractions: AttractionResponse[]
}
export const attractionListResponseFromJson = (json: string): AttractionListResponse =>
  JSON.parse(json) as AttractionListResponse

export const attractionListResponseToJson = (value: AttractionListResponse): string =>
  JSON.stringify(value)
