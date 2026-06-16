// 本文件定义 `ListManagerTasksPlanner` 的前端入口，负责把任务列表查询转给后端。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerTasksPlannerRequest } from '../objects/ManagerTasksPlannerRequest'
import type { ManagerTaskListResponse } from '../objects/ManagerTaskListResponse'

export const listManagerTasksPlanner = (input: ManagerTasksPlannerRequest): Promise<ManagerTaskListResponse> =>
  executeJsonApiRequest('/ListManagerTasksPlanner', 'POST', input)
