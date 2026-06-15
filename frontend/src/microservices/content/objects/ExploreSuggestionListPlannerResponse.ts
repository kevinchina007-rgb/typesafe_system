// ExploreSuggestionListPlannerResponse：content 域探索推荐列表返回对象。

import type { ExploreSuggestionPlannerResponse } from './ExploreSuggestionPlannerResponse'
export type ExploreSuggestionListPlannerResponse = {
  suggestions: ExploreSuggestionPlannerResponse[]
}
export const exploreSuggestionListPlannerResponseFromJson = (json: string): ExploreSuggestionListPlannerResponse =>
  JSON.parse(json) as ExploreSuggestionListPlannerResponse
export const exploreSuggestionListPlannerResponseToJson = (value: ExploreSuggestionListPlannerResponse): string =>
  JSON.stringify(value)
