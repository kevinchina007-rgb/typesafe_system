// 本文件定义 content 模块的 `ReviewResponse`，作为响应数据并提供 JSON 编解码。

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
export const reviewResponseFromJson = (json: string): ReviewResponse =>
  JSON.parse(json) as ReviewResponse

export const reviewResponseToJson = (value: ReviewResponse): string =>
  JSON.stringify(value)
