import type { SearchSuggestionResponse } from './SearchSuggestionResponse'

// 搜索建议列表接口返回的数据结构。
export type SearchSuggestionListResponse = {
  suggestions: SearchSuggestionResponse[]
}

// 把搜索建议列表 JSON 解析成对象。
export const searchSuggestionListResponseFromJson = (json: string): SearchSuggestionListResponse =>
  JSON.parse(json) as SearchSuggestionListResponse

// 把搜索建议列表对象序列化成 JSON。
export const searchSuggestionListResponseToJson = (value: SearchSuggestionListResponse): string =>
  JSON.stringify(value)
