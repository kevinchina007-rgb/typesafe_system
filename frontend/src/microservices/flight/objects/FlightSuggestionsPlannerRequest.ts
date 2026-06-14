// 本文件定义 flight 模块的 `FlightSuggestionsPlannerRequest`，作为 planner 请求参数并提供 JSON 编解码。

export type FlightSuggestionsPlannerRequest = {
  q: string
}

export const flightSuggestionsPlannerRequestFromJson = (json: string): FlightSuggestionsPlannerRequest =>
  JSON.parse(json) as FlightSuggestionsPlannerRequest

export const flightSuggestionsPlannerRequestToJson = (value: FlightSuggestionsPlannerRequest): string =>
  JSON.stringify(value)

