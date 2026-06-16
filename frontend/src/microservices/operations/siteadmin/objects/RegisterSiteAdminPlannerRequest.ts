// 本文件定义 RegisterSiteAdminPlanner 的站点管理员注册请求，与后端同名字段对齐。

export type RegisterSiteAdminPlannerRequest = {
  email: string
  displayName: string
  password: string
}

export const registerSiteAdminPlannerRequestFromJson = (json: string): RegisterSiteAdminPlannerRequest =>
  JSON.parse(json) as RegisterSiteAdminPlannerRequest

export const registerSiteAdminPlannerRequestToJson = (value: RegisterSiteAdminPlannerRequest): string =>
  JSON.stringify(value)
