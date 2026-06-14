// 本文件定义 flight 模块的 `FlightListPlannerResponse`，作为列表响应数据并提供 JSON 编解码。

import type { FlightPlannerResponse } from "./FlightPlannerResponse"

export type FlightListPlannerResponse = {
  flights: FlightPlannerResponse[]
}

export const flightListPlannerResponseFromJson = (json: string): FlightListPlannerResponse =>
  JSON.parse(json) as FlightListPlannerResponse

export const flightListPlannerResponseToJson = (value: FlightListPlannerResponse): string =>
  JSON.stringify(value)