// 鏈枃浠跺畾涔?RejectManagerBookingItemPlanner锛岃礋璐?operations 妯″潡鐨勯┏鍥炵紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const rejectManagerBookingItem = (
  orderItemId: string,
  payload: { managerId: string; managerType: string; reason: string },
): Promise<unknown> =>
  executeJsonApiRequest('/RejectManagerBookingItemPlanner', 'POST', { ...payload, orderItemId })

