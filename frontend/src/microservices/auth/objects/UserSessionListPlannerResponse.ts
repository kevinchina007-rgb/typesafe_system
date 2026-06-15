// 本文件定义后端 UserSessionListPlannerResponse 对应的用户会话列表响应结构，并提供 JSON 编解码。

import type { UserSessionPlannerResponse } from './UserSessionPlannerResponse'

export type UserSessionListPlannerResponse = {
  sessions: UserSessionPlannerResponse[]
}

export const userSessionListPlannerResponseFromJson = (json: string): UserSessionListPlannerResponse =>
  JSON.parse(json) as UserSessionListPlannerResponse

export const userSessionListPlannerResponseToJson = (value: UserSessionListPlannerResponse): string =>
  JSON.stringify(value)
