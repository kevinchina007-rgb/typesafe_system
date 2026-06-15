// 本文件定义 BatchRejectManagerTasksPlanner，负责 operations 模块的批量驳回编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerBatchDecisionResponse } from '@/microservices/operations/objects/ManagerBatchDecisionResponse'

export const batchRejectManagerBookingItems = (payload: {
  managerId: string
  managerType: string
  orderItemIds: string[]
  reason: string
}): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/BatchRejectManagerTasksPlanner', 'POST', payload)
