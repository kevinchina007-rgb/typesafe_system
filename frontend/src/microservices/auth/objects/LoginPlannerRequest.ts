// 本文件定义后端 LoginPlanner 对应的登录请求结构，并提供 JSON 编解码。

export type LoginPlannerRequest = {
  email: string
  password: string
}

export const loginPlannerRequestFromJson = (json: string): LoginPlannerRequest =>
  JSON.parse(json) as LoginPlannerRequest

export const loginPlannerRequestToJson = (value: LoginPlannerRequest): string =>
  JSON.stringify(value)
