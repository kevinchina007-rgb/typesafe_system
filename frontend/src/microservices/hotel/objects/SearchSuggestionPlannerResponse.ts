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
