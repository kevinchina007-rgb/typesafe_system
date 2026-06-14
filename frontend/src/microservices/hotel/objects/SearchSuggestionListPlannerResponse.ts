import type { SearchSuggestionPlannerResponse } from './SearchSuggestionPlannerResponse'

export type SearchSuggestionListPlannerResponse = {
  suggestions: SearchSuggestionPlannerResponse[]
}

export const searchSuggestionListPlannerResponseFromJson = (json: string): SearchSuggestionListPlannerResponse =>
  JSON.parse(json) as SearchSuggestionListPlannerResponse

export const searchSuggestionListPlannerResponseToJson = (value: SearchSuggestionListPlannerResponse): string =>
  JSON.stringify(value)
