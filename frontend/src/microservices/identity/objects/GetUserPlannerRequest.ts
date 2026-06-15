// 本文件定义 identity 获取用户接口对应的请求对象，只包含用户标识，并提供 JSON 编解码。
export type GetUserPlannerRequest = {
  userId: string
}

export const getUserPlannerRequestFromJson = (json: string): GetUserPlannerRequest =>
  JSON.parse(json) as GetUserPlannerRequest

export const getUserPlannerRequestToJson = (value: GetUserPlannerRequest): string =>
  JSON.stringify(value)
