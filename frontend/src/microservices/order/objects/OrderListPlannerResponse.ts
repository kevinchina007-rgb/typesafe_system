// 本文件定义订单列表响应，和后端 `OrderListPlannerResponse` 保持同名镜像。

import type { OrderPlannerResponse } from './OrderPlannerResponse'

export type OrderListPlannerResponse = {
  orders: OrderPlannerResponse[]
}

export const orderListPlannerResponseFromJson = (json: string): OrderListPlannerResponse =>
  JSON.parse(json) as OrderListPlannerResponse

export const orderListPlannerResponseToJson = (value: OrderListPlannerResponse): string =>
  JSON.stringify(value)
