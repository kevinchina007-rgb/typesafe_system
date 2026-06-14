// 本文件定义 attraction 模块的 `AttractionSuggestionPlannerResponse`，作为单条建议响应并提供 JSON 编解码。

export type AttractionSuggestionPlannerResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

export const attractionSuggestionPlannerResponseFromJson = (json: string): AttractionSuggestionPlannerResponse =>
  JSON.parse(json) as AttractionSuggestionPlannerResponse

export const attractionSuggestionPlannerResponseToJson = (value: AttractionSuggestionPlannerResponse): string =>
  JSON.stringify(value)
