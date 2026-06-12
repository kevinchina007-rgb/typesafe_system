// 本文件定义 operations 模块的 `ManagerFlightOrderListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { ManagerFlightOrderResponse } from './ManagerFlightOrderResponse'

export type ManagerFlightOrderListResponse = {
  orders: ManagerFlightOrderResponse[]
}

export const managerFlightOrderListResponseFromJson = (json: string): ManagerFlightOrderListResponse =>
  JSON.parse(json) as ManagerFlightOrderListResponse

export const managerFlightOrderListResponseToJson = (value: ManagerFlightOrderListResponse): string =>
  JSON.stringify(value)
