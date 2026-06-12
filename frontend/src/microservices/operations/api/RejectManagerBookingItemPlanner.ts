// 本文件定义 RejectManagerBookingItemPlanner，负责 operations 模块的驳回编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const rejectManagerBookingItem = (
  orderItemId: string,
  payload: { managerId: string; managerType: string; reason: string },
): Promise<unknown> =>
  executeJsonApiRequest('/RejectManagerBookingItemPlanner', 'POST', { ...payload, orderItemId })
