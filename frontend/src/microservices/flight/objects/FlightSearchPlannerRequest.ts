// 本文件定义 flight 模块的 `FlightSearchPlannerRequest`，作为 planner 请求参数并提供 JSON 编解码。

export type FlightSearchPlannerRequest = {
  departureAirport?: string
  arrivalAirport?: string
  date?: string
}

export const flightSearchPlannerRequestFromJson = (json: string): FlightSearchPlannerRequest =>
  JSON.parse(json) as FlightSearchPlannerRequest

export const flightSearchPlannerRequestToJson = (value: FlightSearchPlannerRequest): string =>
  JSON.stringify(value)