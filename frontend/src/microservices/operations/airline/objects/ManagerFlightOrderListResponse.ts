// 本文件定义 operations 航空管理端的 `ManagerFlightOrderListResponse`，作为订单列表响应数据并提供 JSON 编解码。

import type { ManagerFlightOrderResponse } from './ManagerFlightOrderResponse'

export type ManagerFlightOrderListResponse = {
  orders: ManagerFlightOrderResponse[]
}

export const managerFlightOrderListResponseFromJson = (json: string): ManagerFlightOrderListResponse =>
  JSON.parse(json) as ManagerFlightOrderListResponse

export const managerFlightOrderListResponseToJson = (value: ManagerFlightOrderListResponse): string =>
  JSON.stringify(value)

