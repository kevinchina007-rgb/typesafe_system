// 本文件定义 attraction 模块的 `AttractionSearchQuery`，作为查询条件并提供 JSON 编解码。

export type AttractionSearchQuery = {
  city?: string
  keyword?: string
  useDate?: string
}

export const attractionSearchQueryFromJson = (json: string): AttractionSearchQuery =>
  JSON.parse(json) as AttractionSearchQuery

export const attractionSearchQueryToJson = (value: AttractionSearchQuery): string =>
  JSON.stringify(value)
