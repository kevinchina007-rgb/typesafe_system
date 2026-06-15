// ListReviewsByResourcePlanner：content 域资源评论列表入口。

import type { ListReviewsByResourcePlannerRequest } from '@/microservices/content/objects/ListReviewsByResourcePlannerRequest'
import type { ReviewListPlannerResponse } from '@/microservices/content/objects/ReviewListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listReviewsByResource = (payload: ListReviewsByResourcePlannerRequest): Promise<ReviewListPlannerResponse> =>
  executeJsonApiRequest('/ListReviewsByResourcePlanner', 'POST', payload)
