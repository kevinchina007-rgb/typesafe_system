import type { TourGroupSummaryResponse } from './TourGroupSummaryResponse'

export type TourGroupListResponse = {
  groups: TourGroupSummaryResponse[]
}
export const tourGroupListResponseFromJson = (json: string): TourGroupListResponse =>
  JSON.parse(json) as TourGroupListResponse

export const tourGroupListResponseToJson = (value: TourGroupListResponse): string =>
  JSON.stringify(value)
