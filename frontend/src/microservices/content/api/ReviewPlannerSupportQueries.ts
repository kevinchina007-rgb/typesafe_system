import type { ResourceReviewSummaryResponse } from '@/microservices/content/objects/ResourceReviewSummaryResponse'
import type { ReviewEligibilityResponse } from '@/microservices/content/objects/ReviewEligibilityResponse'
import type { ReviewListResponse } from '@/microservices/content/objects/ReviewListResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listMyReviews = (userId: string): Promise<ReviewListResponse> =>
  executeJsonApiRequest('/ListMyReviewsPlanner', 'POST', { userId })

export const listReviewsByResource = (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ReviewListResponse> =>
  executeJsonApiRequest('/ListReviewsByResourcePlanner', 'POST', payload)

export const getReviewResourceSummary = (payload: { userId: string; resourceType: string; resourceId: string }): Promise<ResourceReviewSummaryResponse> =>
  executeJsonApiRequest('/GetReviewSummaryPlanner', 'POST', payload)

export const getReviewEligibility = (payload: { userId: string; orderItemId: string }): Promise<ReviewEligibilityResponse> =>
  executeJsonApiRequest('/CheckReviewEligibilityPlanner', 'POST', payload)

