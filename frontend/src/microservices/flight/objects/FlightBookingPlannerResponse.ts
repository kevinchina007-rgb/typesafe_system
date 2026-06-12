// 本文件定义 flight 模块的 `FlightBookingPlannerResponse`，作为planner 响应数据并提供 JSON 编解码。

export type FlightBookingPlannerResponse = {
  orderId: string
  orderItemId: string
  status: string
  totalPriceAmount: string
  currency: string
}

export const flightBookingPlannerResponseFromJson = (json: string): FlightBookingPlannerResponse =>
  JSON.parse(json) as FlightBookingPlannerResponse

export const flightBookingPlannerResponseToJson = (value: FlightBookingPlannerResponse): string =>
  JSON.stringify(value)
