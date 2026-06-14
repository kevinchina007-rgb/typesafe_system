// 本文件定义 attraction 模块的 `AttractionSuggestionRequest`，作为建议查询参数并提供 JSON 编解码。

export type AttractionSuggestionRequest = {
  q: string
}

export const attractionSuggestionRequestFromJson = (json: string): AttractionSuggestionRequest =>
  JSON.parse(json) as AttractionSuggestionRequest

export const attractionSuggestionRequestToJson = (value: AttractionSuggestionRequest): string =>
  JSON.stringify(value)
