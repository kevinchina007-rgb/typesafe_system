// 本文件定义 `BatchRejectManagerTasksPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerBatchDecisionPlannerRequest } from '../objects/ManagerBatchDecisionPlannerRequest'
import type { ManagerBatchDecisionResponse } from '../objects/ManagerBatchDecisionResponse'

export const batchRejectManagerTasksPlanner = (input: ManagerBatchDecisionPlannerRequest): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/BatchRejectManagerTasksPlanner', 'POST', input)
