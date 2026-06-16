// 本文件定义 RegisterAirlineManagerPlanner 的航空管理员注册请求。

export type RegisterAirlineManagerPlannerRequest = {
  email: string
  displayName: string
  airlineName: string
  airlineCode: string
  password: string
}

export const registerAirlineManagerPlannerRequestFromJson = (json: string): RegisterAirlineManagerPlannerRequest =>
  JSON.parse(json) as RegisterAirlineManagerPlannerRequest

export const registerAirlineManagerPlannerRequestToJson = (value: RegisterAirlineManagerPlannerRequest): string =>
  JSON.stringify(value)
