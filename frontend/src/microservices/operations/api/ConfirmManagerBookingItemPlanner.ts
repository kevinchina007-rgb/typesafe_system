// 本文件定义 ConfirmManagerBookingItemPlanner，负责 operations 模块的确认编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const confirmManagerBookingItem = (
  orderItemId: string,
  payload: { managerId: string; managerType: string; note?: string | null },
): Promise<unknown> =>
  executeJsonApiRequest('/ConfirmManagerBookingItemPlanner', 'POST', { ...payload, orderItemId })
