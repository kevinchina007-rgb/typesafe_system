import type { ExploreSearchResultResponse } from './ExploreSearchResultResponse'

export type ExploreSearchResponse = {
  results: ExploreSearchResultResponse[]
}
export const exploreSearchResponseFromJson = (json: string): ExploreSearchResponse =>
  JSON.parse(json) as ExploreSearchResponse

export const exploreSearchResponseToJson = (value: ExploreSearchResponse): string =>
  JSON.stringify(value)
