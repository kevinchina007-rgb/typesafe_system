// 本文件定义 BatchConfirmManagerTasksPlanner，负责 operations 模块的批量确认编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { ManagerBatchDecisionResponse } from '@/microservices/operations/objects/ManagerBatchDecisionResponse'

export const batchConfirmManagerBookingItems = (payload: {
  managerId: string
  managerType: string
  orderItemIds: string[]
  note?: string | null
}): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/BatchConfirmManagerTasksPlanner', 'POST', payload)
