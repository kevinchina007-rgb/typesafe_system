// 本文件定义 ListManagerTasksPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerTaskListResponse } from '@/microservices/operations/objects/ManagerTaskListResponse'

export const listManagerTasks = (query: { managerId: string; managerType: string; status?: string; resourceType?: string }): Promise<ManagerTaskListResponse> =>
  executeJsonApiRequest('/ListManagerTasksPlanner', 'POST', {
    managerId: query.managerId,
    managerType: query.managerType,
    taskStatus: query.status,
    taskResourceType: query.resourceType,
  })
