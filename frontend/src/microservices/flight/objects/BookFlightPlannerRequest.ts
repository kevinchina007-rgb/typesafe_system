// 本文件定义 flight 模块的 `BookFlightPlannerRequest`，作为planner 请求参数并提供 JSON 编解码。

export type { BookFlightPlannerRequest } from './BookFlightRequest'
export { bookFlightRequestFromJson, bookFlightRequestToJson } from './BookFlightRequest'
