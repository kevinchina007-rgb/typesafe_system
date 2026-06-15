// 本文件定义后端 ChangePasswordPlanner 对应的修改密码请求结构，并提供 JSON 编解码。

export type ChangePasswordPlannerRequest = {
  currentPassword: string
  newPassword: string
}

export const changePasswordPlannerRequestFromJson = (json: string): ChangePasswordPlannerRequest =>
  JSON.parse(json) as ChangePasswordPlannerRequest

export const changePasswordPlannerRequestToJson = (value: ChangePasswordPlannerRequest): string =>
  JSON.stringify(value)
