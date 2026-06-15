// ReviewListPlannerResponse：content 域评论列表返回对象。

import type { ReviewPlannerResponse } from './ReviewPlannerResponse'
export type ReviewListPlannerResponse = {
  reviews: ReviewPlannerResponse[]
}
export const reviewListPlannerResponseFromJson = (json: string): ReviewListPlannerResponse =>
  JSON.parse(json) as ReviewListPlannerResponse
export const reviewListPlannerResponseToJson = (value: ReviewListPlannerResponse): string =>
  JSON.stringify(value)
