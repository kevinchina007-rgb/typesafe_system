// 鏈枃浠跺畾涔?BatchRejectManagerTasksPlanner锛岃礋璐?operations 妯″潡鐨勬壒閲忛┏鍥炵紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerBatchDecisionResponse } from '@/microservices/operations/overall/objects/ManagerBatchDecisionResponse'

export const batchRejectManagerBookingItems = (payload: {
  managerId: string
  managerType: string
  orderItemIds: string[]
  reason: string
}): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/BatchRejectManagerTasksPlanner', 'POST', payload)


