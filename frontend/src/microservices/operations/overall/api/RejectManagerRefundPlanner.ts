// 鏈枃浠跺畾涔?RejectManagerRefundPlanner锛岃礋璐?operations 妯″潡鐨勯┏鍥炵紪鎺掑拰鎺ュ彛鍏ュ彛銆?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const rejectRefund = (_orderId: string, managerId: string, managerType: string): Promise<unknown> =>
  executeJsonApiRequest('/RejectManagerRefundPlanner', 'POST', { managerId, managerType })

