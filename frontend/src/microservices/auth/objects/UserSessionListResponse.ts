import type { UserSessionListPlannerResponse } from './UserSessionListPlannerResponse'
import type { UserSessionResponse } from './UserSessionResponse'
import { userSessionResponseFromPlannerResponse } from './UserSessionResponse'

// 本文件保留为兼容层，把后端会话列表转换成前端还在使用的“带当前会话标记”结构。
export type UserSessionListResponse = {
  sessions: UserSessionResponse[]
}

export function userSessionListResponseFromPlannerResponse(
  plannerResponse: UserSessionListPlannerResponse,
  currentSessionId: string | null,
): UserSessionListResponse {
  return {
    sessions: plannerResponse.sessions.map(session => userSessionResponseFromPlannerResponse(session, session.sessionId === currentSessionId)),
  }
}

export const userSessionListResponseFromJson = (json: string): UserSessionListResponse =>
  JSON.parse(json) as UserSessionListResponse

export const userSessionListResponseToJson = (value: UserSessionListResponse): string =>
  JSON.stringify(value)
