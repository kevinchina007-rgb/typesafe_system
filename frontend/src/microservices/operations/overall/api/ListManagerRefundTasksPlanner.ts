// 鏈枃浠跺畾涔?ListManagerRefundTasksPlanner锛岃礋璐?operations 妯″潡鐨勫垪琛ㄦ煡璇㈢紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerRefundTaskListResponse } from '@/microservices/operations/overall/objects/ManagerRefundTaskListResponse'

export const listManagerRefundTasks = (query: { managerId: string; managerType: string }): Promise<ManagerRefundTaskListResponse> =>
  executeJsonApiRequest('/ListManagerRefundTasksPlanner', 'POST', query)


