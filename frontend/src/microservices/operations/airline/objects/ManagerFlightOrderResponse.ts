// 本文件定义 operations 航空管理端的 `ManagerFlightOrderResponse`，作为订单响应数据并提供 JSON 编解码。

import type { ManagerFlightOrderTravelerResponse } from './ManagerFlightOrderTravelerResponse'

export type ManagerFlightOrderResponse = {
  orderId: string
  orderItemId: string
  buyerUserId: string
  buyerNickname: string
  cabinClass: string
  orderStatus: string
  orderCreatedAt: string
  travelerIds: string[]
  travelers: ManagerFlightOrderTravelerResponse[]
}

export const managerFlightOrderResponseFromJson = (json: string): ManagerFlightOrderResponse =>
  JSON.parse(json) as ManagerFlightOrderResponse

export const managerFlightOrderResponseToJson = (value: ManagerFlightOrderResponse): string =>
  JSON.stringify(value)

