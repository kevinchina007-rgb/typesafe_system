// 本文件定义后端 ManagerChangePasswordPlanner 对应的管理员修改密码请求结构，并提供 JSON 编解码。

export type ManagerChangePasswordPlannerRequest = {
  currentPassword: string
  newPassword: string
}

export const managerChangePasswordPlannerRequestFromJson = (json: string): ManagerChangePasswordPlannerRequest =>
  JSON.parse(json) as ManagerChangePasswordPlannerRequest

export const managerChangePasswordPlannerRequestToJson = (value: ManagerChangePasswordPlannerRequest): string =>
  JSON.stringify(value)
