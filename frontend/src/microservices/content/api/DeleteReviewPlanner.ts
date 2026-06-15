// DeleteReviewPlanner：content 域删除评论入口。

import type { DeleteReviewPlannerRequest } from '@/microservices/content/objects/DeleteReviewPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const deleteReview = (payload: DeleteReviewPlannerRequest): Promise<void> =>
  executeJsonApiRequest('/DeleteReviewPlanner', 'POST', payload)
