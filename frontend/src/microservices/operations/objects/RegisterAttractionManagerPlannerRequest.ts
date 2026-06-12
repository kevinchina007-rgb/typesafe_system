// 本文件定义 operations 模块的 `RegisterAttractionManagerPlannerRequest`，作为planner 请求参数并提供 JSON 编解码。

export type RegisterAttractionManagerPlannerRequest = {
  email: string
  displayName: string
  password: string
}

export const registerAttractionManagerPlannerRequestFromJson = (json: string): RegisterAttractionManagerPlannerRequest =>
  JSON.parse(json) as RegisterAttractionManagerPlannerRequest

export const registerAttractionManagerPlannerRequestToJson = (value: RegisterAttractionManagerPlannerRequest): string =>
  JSON.stringify(value)
