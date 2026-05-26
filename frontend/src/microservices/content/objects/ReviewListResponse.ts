import type { ReviewResponse } from './ReviewResponse'

export type ReviewListResponse = {
  reviews: ReviewResponse[]
}
export const reviewListResponseFromJson = (json: string): ReviewListResponse =>
  JSON.parse(json) as ReviewListResponse

export const reviewListResponseToJson = (value: ReviewListResponse): string =>
  JSON.stringify(value)
