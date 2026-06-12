import type { ExploreSearchResultResponse } from './ExploreSearchResultResponse'

// 探索页搜索接口返回的资源列表。
export type ExploreSearchResponse = {
  results: ExploreSearchResultResponse[]
}

// 把探索搜索结果 JSON 解析成对象。
export const exploreSearchResponseFromJson = (json: string): ExploreSearchResponse =>
  JSON.parse(json) as ExploreSearchResponse

// 把探索搜索结果对象序列化成 JSON。
export const exploreSearchResponseToJson = (value: ExploreSearchResponse): string =>
  JSON.stringify(value)
