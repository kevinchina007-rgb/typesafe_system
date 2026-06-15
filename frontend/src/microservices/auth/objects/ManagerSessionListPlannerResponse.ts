// 本文件定义后端 ManagerSessionListPlannerResponse 对应的管理员会话列表响应结构，并提供 JSON 编解码。

import type { ManagerSessionPlannerResponse } from './ManagerSessionPlannerResponse'

export type ManagerSessionListPlannerResponse = {
  sessions: ManagerSessionPlannerResponse[]
}

export const managerSessionListPlannerResponseFromJson = (json: string): ManagerSessionListPlannerResponse =>
  JSON.parse(json) as ManagerSessionListPlannerResponse

export const managerSessionListPlannerResponseToJson = (value: ManagerSessionListPlannerResponse): string =>
  JSON.stringify(value)
