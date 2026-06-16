// 本文件定义 operations 航空管理端的 `UpdateAirlineManagerProfilePlannerRequest`，作为 planner 请求参数并提供 JSON 编解码。

export type UpdateAirlineManagerProfilePlannerRequest = {
  managerId: string
  displayName: string
  airlineName: string
  airlineCode: string
  logoAssetPath?: string | null
}

export const updateAirlineManagerProfilePlannerRequestFromJson = (json: string): UpdateAirlineManagerProfilePlannerRequest =>
  JSON.parse(json) as UpdateAirlineManagerProfilePlannerRequest

export const updateAirlineManagerProfilePlannerRequestToJson = (value: UpdateAirlineManagerProfilePlannerRequest): string =>
  JSON.stringify(value)

