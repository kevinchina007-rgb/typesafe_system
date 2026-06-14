// 本文件定义 flight 模块的 `GetFlightDetailsPlannerRequest`，作为 planner 请求参数并提供 JSON 编解码。

export type GetFlightDetailsPlannerRequest = {
  flightId: string
}

export const getFlightDetailsPlannerRequestFromJson = (json: string): GetFlightDetailsPlannerRequest =>
  JSON.parse(json) as GetFlightDetailsPlannerRequest

export const getFlightDetailsPlannerRequestToJson = (value: GetFlightDetailsPlannerRequest): string =>
  JSON.stringify(value)

