// ExploreSearchResultPlannerResponse：content 域探索搜索单条结果对象。

export type ExploreSearchResultPlannerResponse = {
  resourceType: string
  resourceId: string
  title: string
  summary: string
  metaLabel: string
  navigationHint: string
  imageUrl: string | null
}
export const exploreSearchResultPlannerResponseFromJson = (json: string): ExploreSearchResultPlannerResponse =>
  JSON.parse(json) as ExploreSearchResultPlannerResponse
export const exploreSearchResultPlannerResponseToJson = (value: ExploreSearchResultPlannerResponse): string =>
  JSON.stringify(value)
