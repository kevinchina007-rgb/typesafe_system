// 本文件定义 train 模块的 `RegisterRailwayManagerPlannerRequest`，用于注册铁路管理员并提供 JSON 编解码。

export type RegisterRailwayManagerPlannerRequest = {
  operatorCode: string
  email: string
  displayName: string
  password: string
}
export const registerRailwayManagerPlannerRequestFromJson = (json: string): RegisterRailwayManagerPlannerRequest =>
  JSON.parse(json) as RegisterRailwayManagerPlannerRequest

export const registerRailwayManagerPlannerRequestToJson = (value: RegisterRailwayManagerPlannerRequest): string =>
  JSON.stringify(value)
