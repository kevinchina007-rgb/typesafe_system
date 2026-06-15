// 本文件定义 ApproveManagerRefundPlanner，负责 operations 模块的审批通过编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const approveRefund = (_orderId: string, managerId: string, managerType: string): Promise<unknown> =>
  executeJsonApiRequest('/ApproveManagerRefundPlanner', 'POST', { managerId, managerType })
