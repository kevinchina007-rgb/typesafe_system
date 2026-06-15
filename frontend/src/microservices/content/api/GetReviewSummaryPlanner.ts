// GetReviewSummaryPlanner：content 域资源评论摘要入口。

import type { GetReviewSummaryPlannerRequest } from '@/microservices/content/objects/GetReviewSummaryPlannerRequest'
import type { ResourceReviewSummaryPlannerResponse } from '@/microservices/content/objects/ResourceReviewSummaryPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const getReviewResourceSummary = (payload: GetReviewSummaryPlannerRequest): Promise<ResourceReviewSummaryPlannerResponse> =>
  executeJsonApiRequest('/GetReviewSummaryPlanner', 'POST', payload)
