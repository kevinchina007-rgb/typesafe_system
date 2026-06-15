// 本文件定义后端 ManagerLoginPlanner 对应的管理员登录请求结构，并提供 JSON 编解码。

import type { ManagerType } from './ManagerType'

export type ManagerLoginPlannerRequest = {
  managerType: ManagerType
  email: string
  password: string
}

export const managerLoginPlannerRequestFromJson = (json: string): ManagerLoginPlannerRequest =>
  JSON.parse(json) as ManagerLoginPlannerRequest

export const managerLoginPlannerRequestToJson = (value: ManagerLoginPlannerRequest): string =>
  JSON.stringify(value)
