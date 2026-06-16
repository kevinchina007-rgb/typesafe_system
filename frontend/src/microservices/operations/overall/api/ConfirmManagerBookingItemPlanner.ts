// 鏈枃浠跺畾涔?ConfirmManagerBookingItemPlanner锛岃礋璐?operations 妯″潡鐨勭‘璁ょ紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const confirmManagerBookingItem = (
  orderItemId: string,
  payload: { managerId: string; managerType: string; note?: string | null },
): Promise<unknown> =>
  executeJsonApiRequest('/ConfirmManagerBookingItemPlanner', 'POST', { ...payload, orderItemId })

