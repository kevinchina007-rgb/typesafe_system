import type { ManagerFlightOrderResponse } from './ManagerFlightOrderResponse'

export type ManagerFlightOrderListResponse = {
  orders: ManagerFlightOrderResponse[]
}

export const managerFlightOrderListResponseFromJson = (json: string): ManagerFlightOrderListResponse =>
  JSON.parse(json) as ManagerFlightOrderListResponse

export const managerFlightOrderListResponseToJson = (value: ManagerFlightOrderListResponse): string =>
  JSON.stringify(value)
