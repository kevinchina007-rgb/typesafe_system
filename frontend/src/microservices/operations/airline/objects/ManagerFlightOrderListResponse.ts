// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerFlightOrderListResponse`锛屼綔涓哄垪琛ㄥ搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

import type { ManagerFlightOrderResponse } from './ManagerFlightOrderResponse'

export type ManagerFlightOrderListResponse = {
  orders: ManagerFlightOrderResponse[]
}

export const managerFlightOrderListResponseFromJson = (json: string): ManagerFlightOrderListResponse =>
  JSON.parse(json) as ManagerFlightOrderListResponse

export const managerFlightOrderListResponseToJson = (value: ManagerFlightOrderListResponse): string =>
  JSON.stringify(value)

