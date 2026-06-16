// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerFlightOrderResponse`锛屼綔涓哄搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

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

