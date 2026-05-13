import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'








import type { ResourceReviewSummaryResponse } from '@/microservices/content/objects/ResourceReviewSummaryResponse'
import type { ReviewEligibilityResponse } from '@/microservices/content/objects/ReviewEligibilityResponse'
import type { ReviewListResponse } from '@/microservices/content/objects/ReviewListResponse'
import type { ReviewResponse } from '@/microservices/content/objects/ReviewResponse'



import { createQueryString, createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

export const listMyReviews = (userId: string): Promise<ReviewListResponse> =>
    executeApiRequest(`/reviews/mine${createQueryString({ userId })}`)

export const listReviewsByResource = (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ReviewListResponse> =>
    executeApiRequest(`/reviews${createQueryString(payload)}`)

export const getReviewResourceSummary = (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ResourceReviewSummaryResponse> =>
    executeApiRequest(`/reviews/summary${createQueryString(payload)}`)

export const getReviewEligibility = (payload: { userId: string; orderItemId: string }): Promise<ReviewEligibilityResponse> =>
    executeApiRequest(`/reviews/eligibility${createQueryString(payload)}`)

export const createReview = (payload: {
    userId: string
    orderId: string
    orderItemId: string
    rating: number
    title: string
    content: string
    images: ContentImageResponse[]
  }): Promise<ReviewResponse> =>
    executeJsonApiRequest('/reviews', 'POST', payload)

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
    executeJsonApiRequest(`/reviews/${reviewId}`, 'PATCH', payload)

export const uploadReviewImage = (userId: string, imageFile: File): Promise<ContentImageResponse> =>
    executeMultipartApiRequest(
      `/reviews/images${createQueryString({ userId })}`,
      'POST',
      createSingleFileFormData('image', imageFile),
    )

export const deleteReview = (reviewId: string, payload: { userId: string }): Promise<void> =>
    executeJsonApiRequest(`/reviews/${reviewId}`, 'DELETE', payload)
