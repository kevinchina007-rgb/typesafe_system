// 本文件定义 flight 模块的 `FlightListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { FlightPlannerResponse } from './FlightPlannerResponse'

export type FlightListPlannerResponse = {
  flights: FlightPlannerResponse[]
}


export const flightListResponseFromJson = (json: string): FlightListPlannerResponse =>
  JSON.parse(json) as FlightListPlannerResponse

export const flightListResponseToJson = (value: FlightListPlannerResponse): string =>
  JSON.stringify(value)
