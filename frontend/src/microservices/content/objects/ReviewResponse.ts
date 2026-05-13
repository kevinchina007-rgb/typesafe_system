import type { ContentImageResponse } from './ContentImageResponse'

export type ReviewResponse = {
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
  images: ContentImageResponse[]
}
