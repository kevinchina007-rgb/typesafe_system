// 本文件定义 ListManagerRefundTasksPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerRefundTaskListResponse } from '@/microservices/operations/objects/ManagerRefundTaskListResponse'

export const listManagerRefundTasks = (query: { managerId: string; managerType: string }): Promise<ManagerRefundTaskListResponse> =>
  executeJsonApiRequest('/ListManagerRefundTasksPlanner', 'POST', query)
