export type ExploreSearchResultResponse = {
  resourceType: string
  resourceId: string
  title: string
  summary: string
  metaLabel: string
  navigationHint: string
  imageUrl: string | null
}
export const exploreSearchResultResponseFromJson = (json: string): ExploreSearchResultResponse =>
  JSON.parse(json) as ExploreSearchResultResponse

export const exploreSearchResultResponseToJson = (value: ExploreSearchResultResponse): string =>
  JSON.stringify(value)
