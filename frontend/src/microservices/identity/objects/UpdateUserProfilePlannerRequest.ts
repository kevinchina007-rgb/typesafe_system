// 本文件定义 identity 更新用户资料接口对应的请求对象，只包含用户标识、昵称和手机号，并提供 JSON 编解码。
export type UpdateUserProfilePlannerRequest = {
  userId: string
  nickname: string
  phone: string
}

export const updateUserProfilePlannerRequestFromJson = (json: string): UpdateUserProfilePlannerRequest =>
  JSON.parse(json) as UpdateUserProfilePlannerRequest

export const updateUserProfilePlannerRequestToJson = (value: UpdateUserProfilePlannerRequest): string =>
  JSON.stringify(value)
