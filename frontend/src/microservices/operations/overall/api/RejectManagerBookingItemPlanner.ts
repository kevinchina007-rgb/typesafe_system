// 本文件定义 `RejectManagerBookingItemPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerDecisionPlannerRequest } from '../objects/ManagerDecisionPlannerRequest'
import type { ManagerBatchDecisionResponse } from '../objects/ManagerBatchDecisionResponse'

export const rejectManagerBookingItemPlanner = (input: ManagerDecisionPlannerRequest): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/RejectManagerBookingItemPlanner', 'POST', input)
