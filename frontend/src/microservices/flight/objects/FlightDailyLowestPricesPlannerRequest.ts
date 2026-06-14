// 本文件定义 flight 模块的 `FlightDailyLowestPricesPlannerRequest`，作为 planner 请求参数并提供 JSON 编解码。

export type FlightDailyLowestPricesPlannerRequest = {
  departureAirport: string
  arrivalAirport: string
  startDate: string
  days: number
  cabinClass?: string
}

export const flightDailyLowestPricesPlannerRequestFromJson = (json: string): FlightDailyLowestPricesPlannerRequest =>
  JSON.parse(json) as FlightDailyLowestPricesPlannerRequest

export const flightDailyLowestPricesPlannerRequestToJson = (value: FlightDailyLowestPricesPlannerRequest): string =>
  JSON.stringify(value)

