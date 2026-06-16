// 本文件定义 `overall` 模块里多个管理端入口共用的基础请求对象。

export type ManagerScopedPlannerRequest = {
  managerId: string
  managerType: string
}

export const managerScopedPlannerRequestFromJson = (json: string): ManagerScopedPlannerRequest =>
  JSON.parse(json) as ManagerScopedPlannerRequest

export const managerScopedPlannerRequestToJson = (value: ManagerScopedPlannerRequest): string =>
  JSON.stringify(value)
