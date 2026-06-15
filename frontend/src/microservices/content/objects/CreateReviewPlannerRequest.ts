// CreateReviewPlannerRequest：content 域创建评论请求对象。

import type { ContentImagePlannerResponse } from './ContentImagePlannerResponse'

export type CreateReviewPlannerRequest = {
  userId: string
  orderId: string
  orderItemId: string
  rating: number
  title: string
  content: string
  images: ContentImagePlannerResponse[]
}

export const createReviewPlannerRequestFromJson = (json: string): CreateReviewPlannerRequest =>
  JSON.parse(json) as CreateReviewPlannerRequest

export const createReviewPlannerRequestToJson = (value: CreateReviewPlannerRequest): string =>
  JSON.stringify(value)
