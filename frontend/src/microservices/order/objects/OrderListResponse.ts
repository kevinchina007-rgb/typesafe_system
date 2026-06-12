// 本文件定义 order 模块的 `OrderListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { OrderResponse } from './OrderResponse'

export type OrderListResponse = {
  orders: OrderResponse[]
}
export const orderListResponseFromJson = (json: string): OrderListResponse =>
  JSON.parse(json) as OrderListResponse

export const orderListResponseToJson = (value: OrderListResponse): string =>
  JSON.stringify(value)
