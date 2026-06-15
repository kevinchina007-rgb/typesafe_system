// ExploreSearchListPlannerResponse：content 域探索搜索列表返回对象。

import type { ExploreSearchResultPlannerResponse } from './ExploreSearchResultPlannerResponse'
export type ExploreSearchListPlannerResponse = {
  results: ExploreSearchResultPlannerResponse[]
}
export const exploreSearchListPlannerResponseFromJson = (json: string): ExploreSearchListPlannerResponse =>
  JSON.parse(json) as ExploreSearchListPlannerResponse
export const exploreSearchListPlannerResponseToJson = (value: ExploreSearchListPlannerResponse): string =>
  JSON.stringify(value)
