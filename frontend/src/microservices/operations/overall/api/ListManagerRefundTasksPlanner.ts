// 本文件定义 `ListManagerRefundTasksPlanner` 的前端入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerScopedPlannerRequest } from '../objects/ManagerScopedPlannerRequest'
import type { ManagerRefundTaskListResponse } from '../objects/ManagerRefundTaskListResponse'

export const listManagerRefundTasksPlanner = (input: ManagerScopedPlannerRequest): Promise<ManagerRefundTaskListResponse> =>
  executeJsonApiRequest('/ListManagerRefundTasksPlanner', 'POST', input)
