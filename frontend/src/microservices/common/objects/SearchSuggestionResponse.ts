// 单条搜索建议的展示结构。
export type SearchSuggestionResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

// 把单条搜索建议 JSON 解析成对象。
export const searchSuggestionResponseFromJson = (json: string): SearchSuggestionResponse =>
  JSON.parse(json) as SearchSuggestionResponse

// 把单条搜索建议对象序列化成 JSON。
export const searchSuggestionResponseToJson = (value: SearchSuggestionResponse): string =>
  JSON.stringify(value)
