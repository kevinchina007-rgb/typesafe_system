// 本文件定义 RegisterAttractionManagerPlanner 的景点管理员注册请求。

export type RegisterAttractionManagerPlannerRequest = {
  email: string
  displayName: string
  password: string
}

export const registerAttractionManagerPlannerRequestFromJson = (json: string): RegisterAttractionManagerPlannerRequest =>
  JSON.parse(json) as RegisterAttractionManagerPlannerRequest

export const registerAttractionManagerPlannerRequestToJson = (value: RegisterAttractionManagerPlannerRequest): string =>
  JSON.stringify(value)
