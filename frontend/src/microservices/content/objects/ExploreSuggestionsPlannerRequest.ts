// ExploreSuggestionsPlannerRequest：content 域探索推荐请求对象。

export type ExploreSuggestionsPlannerRequest = {
  q: string
}

export const exploreSuggestionsPlannerRequestFromJson = (json: string): ExploreSuggestionsPlannerRequest =>
  JSON.parse(json) as ExploreSuggestionsPlannerRequest

export const exploreSuggestionsPlannerRequestToJson = (value: ExploreSuggestionsPlannerRequest): string =>
  JSON.stringify(value)
