// 本文件定义 content 模块的 ReviewPlannerSupport 共享函数，供多个 planner 复用参数整理和结果映射。

import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'
import type { ResourceReviewSummaryResponse } from '@/microservices/content/objects/ResourceReviewSummaryResponse'
import type { ReviewEligibilityResponse } from '@/microservices/content/objects/ReviewEligibilityResponse'
import type { ReviewListResponse } from '@/microservices/content/objects/ReviewListResponse'
import type { ReviewResponse } from '@/microservices/content/objects/ReviewResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listMyReviews = (userId: string): Promise<ReviewListResponse> =>
  executeJsonApiRequest('/ListMyReviewsPlanner', 'POST', { userId })

export const listReviewsByResource = (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ReviewListResponse> =>
  executeJsonApiRequest('/ListReviewsByResourcePlanner', 'POST', payload)

export const getReviewResourceSummary = (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ResourceReviewSummaryResponse> =>
  executeJsonApiRequest('/GetReviewSummaryPlanner', 'POST', payload)

export const getReviewEligibility = (payload: { userId: string; orderItemId: string }): Promise<ReviewEligibilityResponse> =>
  executeJsonApiRequest('/CheckReviewEligibilityPlanner', 'POST', payload)

export const createReview = (payload: {
  userId: string
  orderId: string
  orderItemId: string
  rating: number
  title: string
  content: string
  images: ContentImageResponse[]
}): Promise<ReviewResponse> =>
  executeJsonApiRequest('/CreateReviewPlanner', 'POST', payload)

export const updateReview = (
  reviewId: string,
  payload: {
    userId: string
    rating: number
    title: string
    content: string
    images: ContentImageResponse[]
  },
): Promise<ReviewResponse> =>
  executeJsonApiRequest('/UpdateReviewPlanner', 'POST', { reviewId, ...payload })

async function toBase64(imageFile: File): Promise<string> {
  const bytes = new Uint8Array(await imageFile.arrayBuffer())
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return window.btoa(binary)
}

export const uploadReviewImage = async (userId: string, imageFile: File): Promise<ContentImageResponse> =>
  executeJsonApiRequest('/UploadReviewImagePlanner', 'POST', {
    userId,
    originalFileName: imageFile.name,
    contentType: imageFile.type || 'application/octet-stream',
    base64Content: await toBase64(imageFile),
  })

export const deleteReview = (reviewId: string, payload: { userId: string }): Promise<void> =>
  executeJsonApiRequest('/DeleteReviewPlanner', 'POST', { reviewId, userId: payload.userId })
