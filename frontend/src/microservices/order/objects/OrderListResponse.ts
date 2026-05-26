import type { OrderResponse } from './OrderResponse'

export type OrderListResponse = {
  orders: OrderResponse[]
}
export const orderListResponseFromJson = (json: string): OrderListResponse =>
  JSON.parse(json) as OrderListResponse

export const orderListResponseToJson = (value: OrderListResponse): string =>
  JSON.stringify(value)
