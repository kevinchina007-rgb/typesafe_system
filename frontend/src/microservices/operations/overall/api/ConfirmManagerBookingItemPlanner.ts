// 本文件定义 `ConfirmManagerBookingItemPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerDecisionPlannerRequest } from '../objects/ManagerDecisionPlannerRequest'
import type { ManagerBatchDecisionResponse } from '../objects/ManagerBatchDecisionResponse'

export const confirmManagerBookingItemPlanner = (input: ManagerDecisionPlannerRequest): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/ConfirmManagerBookingItemPlanner', 'POST', input)
