// 本文件定义 identity 登录接口对应的请求对象，只包含登录所需的邮箱字段，并提供 JSON 编解码。
export type LoginPlannerRequest = {
  email: string
}

export const loginPlannerRequestFromJson = (json: string): LoginPlannerRequest =>
  JSON.parse(json) as LoginPlannerRequest

export const loginPlannerRequestToJson = (value: LoginPlannerRequest): string =>
  JSON.stringify(value)
