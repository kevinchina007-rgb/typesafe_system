// 本文件定义 `RejectManagerRefundPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerRefundDecisionPlannerRequest } from '../objects/ManagerRefundDecisionPlannerRequest'
import type { ManagerBatchDecisionResponse } from '../objects/ManagerBatchDecisionResponse'

export const rejectManagerRefundPlanner = (input: ManagerRefundDecisionPlannerRequest): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/RejectManagerRefundPlanner', 'POST', input)
