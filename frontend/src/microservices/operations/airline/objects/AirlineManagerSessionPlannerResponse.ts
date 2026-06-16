// 本文件定义 airline 子域自己的管理员会话返回体。
// 它对应 RegisterAirlineManagerPlanner / UpdateAirlineManagerProfilePlanner 的返回数据，不再借用 overall 子域。
export type AirlineManagerSessionPlannerResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  logoAssetPath?: string | null
  createdAt: string
}

export const airlineManagerSessionPlannerResponseFromJson = (json: string): AirlineManagerSessionPlannerResponse =>
  JSON.parse(json) as AirlineManagerSessionPlannerResponse

export const airlineManagerSessionPlannerResponseToJson = (value: AirlineManagerSessionPlannerResponse): string =>
  JSON.stringify(value)
