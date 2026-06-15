// 本文件定义后端 SignupPlanner 对应的注册请求结构，并提供 JSON 编解码。

export type SignupPlannerRequest = {
  email: string
  nickname: string
  phone: string
  password: string
}

export const signupPlannerRequestFromJson = (json: string): SignupPlannerRequest =>
  JSON.parse(json) as SignupPlannerRequest

export const signupPlannerRequestToJson = (value: SignupPlannerRequest): string =>
  JSON.stringify(value)
