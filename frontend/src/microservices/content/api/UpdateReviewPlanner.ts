// UpdateReviewPlanner：content 域更新评论入口。

import type { UpdateReviewPlannerRequest } from '@/microservices/content/objects/UpdateReviewPlannerRequest'
import type { ReviewPlannerResponse } from '@/microservices/content/objects/ReviewPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const updateReview = (payload: UpdateReviewPlannerRequest): Promise<ReviewPlannerResponse> =>
  executeJsonApiRequest('/UpdateReviewPlanner', 'POST', payload)
