// 本文件定义 identity 创建用户接口对应的请求对象，只包含邮箱、昵称和手机号，并提供 JSON 编解码。
export type CreateUserPlannerRequest = {
  email: string
  nickname: string
  phone: string
}

export const createUserPlannerRequestFromJson = (json: string): CreateUserPlannerRequest =>
  JSON.parse(json) as CreateUserPlannerRequest

export const createUserPlannerRequestToJson = (value: CreateUserPlannerRequest): string =>
  JSON.stringify(value)
