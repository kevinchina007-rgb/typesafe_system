// CheckReviewEligibilityPlanner：content 域评论资格判断入口。

import type { CheckReviewEligibilityPlannerRequest } from '@/microservices/content/objects/CheckReviewEligibilityPlannerRequest'
import type { ReviewEligibilityPlannerResponse } from '@/microservices/content/objects/ReviewEligibilityPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getReviewEligibility = (payload: CheckReviewEligibilityPlannerRequest): Promise<ReviewEligibilityPlannerResponse> =>
  executeJsonApiRequest('/CheckReviewEligibilityPlanner', 'POST', payload)
