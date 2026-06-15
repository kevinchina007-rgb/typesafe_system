// 本文件定义 `CreateOrderPlanner`，负责创建订单并直接调用后端同名接口。

import type { CreateOrderPlannerRequest } from '@/microservices/order/objects/CreateOrderPlannerRequest'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const createOrder = (payload: CreateOrderPlannerRequest): Promise<OrderResponse> =>
  executeJsonApiRequest('/CreateOrderPlanner', 'POST', payload)
