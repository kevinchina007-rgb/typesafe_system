// 本文件定义 attraction 模块的 `AttractionSuggestionListPlannerResponse`，作为建议列表响应并提供 JSON 编解码。

import type { AttractionSuggestionPlannerResponse } from './AttractionSuggestionPlannerResponse'

export type AttractionSuggestionListPlannerResponse = {
  suggestions: AttractionSuggestionPlannerResponse[]
}

export const attractionSuggestionListPlannerResponseFromJson = (json: string): AttractionSuggestionListPlannerResponse =>
  JSON.parse(json) as AttractionSuggestionListPlannerResponse

export const attractionSuggestionListPlannerResponseToJson = (value: AttractionSuggestionListPlannerResponse): string =>
  JSON.stringify(value)
