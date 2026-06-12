// 本文件定义 flight 模块的 `FlightSearchQuery`，作为查询条件并提供 JSON 编解码。

export type FlightSearchPlannerRequest = {
  departureAirport?: string
  arrivalAirport?: string
  date?: string
}


export const flightSearchQueryFromJson = (json: string): FlightSearchPlannerRequest =>
  JSON.parse(json) as FlightSearchPlannerRequest

export const flightSearchQueryToJson = (value: FlightSearchPlannerRequest): string =>
  JSON.stringify(value)
