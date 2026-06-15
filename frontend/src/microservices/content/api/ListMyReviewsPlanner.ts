// ListMyReviewsPlanner：content 域个人评论列表入口。

import type { ListMyReviewsPlannerRequest } from '@/microservices/content/objects/ListMyReviewsPlannerRequest'
import type { ReviewListPlannerResponse } from '@/microservices/content/objects/ReviewListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listMyReviews = (payload: ListMyReviewsPlannerRequest): Promise<ReviewListPlannerResponse> =>
  executeJsonApiRequest('/ListMyReviewsPlanner', 'POST', payload)
