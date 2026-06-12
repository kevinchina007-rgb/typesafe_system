// 本文件定义 flight 模块的 `BookFlightRequest`，作为请求参数并提供 JSON 编解码。

export type BookFlightPlannerRequest = {
  userId: string
  flightId: string
  travelerIds: string[]
  cabinClass: string
}

export const bookFlightRequestFromJson = (json: string): BookFlightPlannerRequest =>
  JSON.parse(json) as BookFlightPlannerRequest

export const bookFlightRequestToJson = (value: BookFlightPlannerRequest): string =>
  JSON.stringify(value)
