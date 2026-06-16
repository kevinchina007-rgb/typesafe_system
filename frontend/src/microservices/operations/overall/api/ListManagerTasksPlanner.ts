// 鏈枃浠跺畾涔?ListManagerTasksPlanner锛岃礋璐?operations 妯″潡鐨勫垪琛ㄦ煡璇㈢紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerTaskListResponse } from '@/microservices/operations/overall/objects/ManagerTaskListResponse'

export const listManagerTasks = (query: { managerId: string; managerType: string; status?: string; resourceType?: string }): Promise<ManagerTaskListResponse> =>
  executeJsonApiRequest('/ListManagerTasksPlanner', 'POST', {
    managerId: query.managerId,
    managerType: query.managerType,
    taskStatus: query.status,
    taskResourceType: query.resourceType,
  })


