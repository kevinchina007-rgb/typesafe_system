import type { SearchSuggestionResponse } from './SearchSuggestionResponse'

export type SearchSuggestionListResponse = {
  suggestions: SearchSuggestionResponse[]
}
export const searchSuggestionListResponseFromJson = (json: string): SearchSuggestionListResponse =>
  JSON.parse(json) as SearchSuggestionListResponse

export const searchSuggestionListResponseToJson = (value: SearchSuggestionListResponse): string =>
  JSON.stringify(value)
