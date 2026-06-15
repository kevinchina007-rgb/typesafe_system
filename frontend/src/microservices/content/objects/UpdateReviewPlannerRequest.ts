// UpdateReviewPlannerRequest：content 域更新评论请求对象。

import type { ContentImagePlannerResponse } from './ContentImagePlannerResponse'

export type UpdateReviewPlannerRequest = {
  userId: string
  reviewId: string
  rating: number
  title: string
  content: string
  images: ContentImagePlannerResponse[]
}

export const updateReviewPlannerRequestFromJson = (json: string): UpdateReviewPlannerRequest =>
  JSON.parse(json) as UpdateReviewPlannerRequest

export const updateReviewPlannerRequestToJson = (value: UpdateReviewPlannerRequest): string =>
  JSON.stringify(value)
