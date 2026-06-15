// ReviewPlannerResponse：content 域单条评论返回对象。

import type { ContentImagePlannerResponse } from './ContentImagePlannerResponse'
export type ReviewPlannerResponse = {
  reviewId: string
  authorUserId: string
  authorDisplayName: string
  authorAvatarUrl: string | null
  resourceType: string
  resourceId: string
  resourceSummaryTitle: string
  resourceSummarySubtitle: string
  orderId: string
  orderItemId: string
  rating: number
  title: string
  content: string
  status: string
  createdAt: string
  updatedAt: string
  isMyReview: boolean
  canEdit: boolean
  canDelete: boolean
  images: ContentImagePlannerResponse[]
}
export const reviewPlannerResponseFromJson = (json: string): ReviewPlannerResponse =>
  JSON.parse(json) as ReviewPlannerResponse
export const reviewPlannerResponseToJson = (value: ReviewPlannerResponse): string =>
  JSON.stringify(value)
