// 鏈枃浠跺畾涔?BatchConfirmManagerTasksPlanner锛岃礋璐?operations 妯″潡鐨勬壒閲忕‘璁ょ紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerBatchDecisionResponse } from '@/microservices/operations/overall/objects/ManagerBatchDecisionResponse'

export const batchConfirmManagerBookingItems = (payload: {
  managerId: string
  managerType: string
  orderItemIds: string[]
  note?: string | null
}): Promise<ManagerBatchDecisionResponse> =>
  executeJsonApiRequest('/BatchConfirmManagerTasksPlanner', 'POST', payload)


