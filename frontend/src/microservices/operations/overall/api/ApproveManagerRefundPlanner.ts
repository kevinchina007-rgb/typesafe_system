// 鏈枃浠跺畾涔?ApproveManagerRefundPlanner锛岃礋璐?operations 妯″潡鐨勫鎵归€氳繃缂栨帓鍜屾帴鍙ｅ叆鍙ｃ€?

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const approveRefund = (_orderId: string, managerId: string, managerType: string): Promise<unknown> =>
  executeJsonApiRequest('/ApproveManagerRefundPlanner', 'POST', { managerId, managerType })

