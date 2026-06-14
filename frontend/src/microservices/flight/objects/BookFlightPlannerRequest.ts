// 本文件定义 flight 模块的 `BookFlightPlannerRequest`，作为 planner 请求参数并提供 JSON 编解码。

export type BookFlightPlannerRequest = {
  userId: string
  flightId: string
  travelerIds: string[]
  cabinClass: string
}

export const bookFlightPlannerRequestFromJson = (json: string): BookFlightPlannerRequest =>
  JSON.parse(json) as BookFlightPlannerRequest

export const bookFlightPlannerRequestToJson = (value: BookFlightPlannerRequest): string =>
  JSON.stringify(value)