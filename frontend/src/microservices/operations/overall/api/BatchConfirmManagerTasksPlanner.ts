// 本文件定义 `BatchConfirmManagerTasksPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerBatchDecisionPlannerRequest } from '../objects/ManagerBatchDecisionPlannerRequest'
import type { ManagerBatchDecisionResponse } from '../objects/ManagerBatchDecisionResponse'

export const batchConfirmManagerTasksPlanner = (input: ManagerBatchDecisionPlannerRequest): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/BatchConfirmManagerTasksPlanner', 'POST', input)
