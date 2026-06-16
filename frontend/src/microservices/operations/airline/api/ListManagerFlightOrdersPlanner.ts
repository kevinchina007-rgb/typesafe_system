// 鏈枃浠跺畾涔?ListManagerFlightOrdersPlanner锛岃礋璐?operations 妯″潡鐨勫垪琛ㄦ煡璇㈢紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerFlightOrderListResponse } from '@/microservices/operations/airline/objects/ManagerFlightOrderListResponse'
import type { ManagerFlightOrdersPlannerRequest } from '@/microservices/operations/airline/objects/ManagerFlightOrdersPlannerRequest'

export const listManagerFlightOrders = (payload: ManagerFlightOrdersPlannerRequest): Promise<ManagerFlightOrderListResponse> =>
  executeJsonApiRequest<ManagerFlightOrderListResponse>('/ListManagerFlightOrdersPlanner', 'POST', payload)

