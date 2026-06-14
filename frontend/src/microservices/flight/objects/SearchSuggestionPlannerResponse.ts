// 本文件定义 flight 模块的 `SearchSuggestionPlannerResponse`，作为单条搜索建议响应并提供 JSON 编解码。

export type SearchSuggestionPlannerResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

export const searchSuggestionPlannerResponseFromJson = (json: string): SearchSuggestionPlannerResponse =>
  JSON.parse(json) as SearchSuggestionPlannerResponse

export const searchSuggestionPlannerResponseToJson = (value: SearchSuggestionPlannerResponse): string =>
  JSON.stringify(value)

