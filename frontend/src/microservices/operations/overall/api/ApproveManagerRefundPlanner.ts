// 本文件定义 `ApproveManagerRefundPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerRefundDecisionPlannerRequest } from '../objects/ManagerRefundDecisionPlannerRequest'
import type { ManagerBatchDecisionResponse } from '../objects/ManagerBatchDecisionResponse'

export const approveManagerRefundPlanner = (input: ManagerRefundDecisionPlannerRequest): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/ApproveManagerRefundPlanner', 'POST', input)
