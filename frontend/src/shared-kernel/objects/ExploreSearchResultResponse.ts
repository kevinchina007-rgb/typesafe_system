// 探索页中单个搜索结果的展示结构。
export type ExploreSearchResultResponse = {
  resourceType: string
  resourceId: string
  title: string
  summary: string
  metaLabel: string
  navigationHint: string
  imageUrl: string | null
}

// 把单个搜索结果 JSON 解析成对象。
export const exploreSearchResultResponseFromJson = (json: string): ExploreSearchResultResponse =>
  JSON.parse(json) as ExploreSearchResultResponse

// 把单个搜索结果对象序列化成 JSON。
export const exploreSearchResultResponseToJson = (value: ExploreSearchResultResponse): string =>
  JSON.stringify(value)
