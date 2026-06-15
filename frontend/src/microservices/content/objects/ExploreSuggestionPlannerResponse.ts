// ExploreSuggestionPlannerResponse：content 域单条探索推荐返回对象。

export type ExploreSuggestionPlannerResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}
export const exploreSuggestionPlannerResponseFromJson = (json: string): ExploreSuggestionPlannerResponse =>
  JSON.parse(json) as ExploreSuggestionPlannerResponse
export const exploreSuggestionPlannerResponseToJson = (value: ExploreSuggestionPlannerResponse): string =>
  JSON.stringify(value)
