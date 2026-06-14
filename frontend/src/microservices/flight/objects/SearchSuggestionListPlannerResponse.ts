// 本文件定义 flight 模块的 `SearchSuggestionListPlannerResponse`，作为搜索建议列表响应并提供 JSON 编解码。

import type { SearchSuggestionPlannerResponse } from './SearchSuggestionPlannerResponse'

export type SearchSuggestionListPlannerResponse = {
  suggestions: SearchSuggestionPlannerResponse[]
}

export const searchSuggestionListPlannerResponseFromJson = (json: string): SearchSuggestionListPlannerResponse =>
  JSON.parse(json) as SearchSuggestionListPlannerResponse

export const searchSuggestionListPlannerResponseToJson = (value: SearchSuggestionListPlannerResponse): string =>
  JSON.stringify(value)

