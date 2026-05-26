export type SearchSuggestionResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}
export const searchSuggestionResponseFromJson = (json: string): SearchSuggestionResponse =>
  JSON.parse(json) as SearchSuggestionResponse

export const searchSuggestionResponseToJson = (value: SearchSuggestionResponse): string =>
  JSON.stringify(value)
