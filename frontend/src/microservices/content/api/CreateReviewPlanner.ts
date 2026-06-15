// CreateReviewPlanner：content 域创建评论入口。

import type { CreateReviewPlannerRequest } from '@/microservices/content/objects/CreateReviewPlannerRequest'
import type { ReviewPlannerResponse } from '@/microservices/content/objects/ReviewPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createReview = (payload: CreateReviewPlannerRequest): Promise<ReviewPlannerResponse> =>
  executeJsonApiRequest('/CreateReviewPlanner', 'POST', payload)
