// 本文件定义 attraction 模块的 `AttractionListPlannerResponse`，作为列表响应数据并提供 JSON 编解码。

import type { AttractionResponse } from './AttractionResponse'

export type AttractionListPlannerResponse = {
  attractions: AttractionResponse[]
}

export const attractionListPlannerResponseFromJson = (json: string): AttractionListPlannerResponse =>
  JSON.parse(json) as AttractionListPlannerResponse

export const attractionListPlannerResponseToJson = (value: AttractionListPlannerResponse): string =>
  JSON.stringify(value)
