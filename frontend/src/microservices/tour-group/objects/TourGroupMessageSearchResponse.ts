import type { TourGroupMessageSearchResultResponse } from './TourGroupMessageSearchResultResponse'

export type TourGroupMessageSearchResponse = {
  results: TourGroupMessageSearchResultResponse[]
}
export const tourGroupMessageSearchResponseFromJson = (json: string): TourGroupMessageSearchResponse =>
  JSON.parse(json) as TourGroupMessageSearchResponse

export const tourGroupMessageSearchResponseToJson = (value: TourGroupMessageSearchResponse): string =>
  JSON.stringify(value)
