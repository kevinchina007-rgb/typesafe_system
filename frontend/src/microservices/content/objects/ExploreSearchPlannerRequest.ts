// ExploreSearchPlannerRequest：content 域探索搜索请求对象。

export type ExploreSearchPlannerRequest = {
  q: string
  resourceType?: string | null
}

export const exploreSearchPlannerRequestFromJson = (json: string): ExploreSearchPlannerRequest =>
  JSON.parse(json) as ExploreSearchPlannerRequest

export const exploreSearchPlannerRequestToJson = (value: ExploreSearchPlannerRequest): string =>
  JSON.stringify(value)
