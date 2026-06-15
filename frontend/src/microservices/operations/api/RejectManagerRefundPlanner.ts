// 本文件定义 RejectManagerRefundPlanner，负责 operations 模块的驳回编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const rejectRefund = (_orderId: string, managerId: string, managerType: string): Promise<unknown> =>
  executeJsonApiRequest('/RejectManagerRefundPlanner', 'POST', { managerId, managerType })
