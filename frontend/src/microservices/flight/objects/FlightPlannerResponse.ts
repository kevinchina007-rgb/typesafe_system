// 本文件定义 flight 模块的 `FlightPlannerResponse`，作为planner 响应数据并提供 JSON 编解码。

export type { FlightPlannerResponse } from './FlightResponse'
export { flightResponseFromJson, flightResponseToJson } from './FlightResponse'
