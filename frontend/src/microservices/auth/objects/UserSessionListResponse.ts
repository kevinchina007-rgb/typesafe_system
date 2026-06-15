import type { UserSessionListPlannerResponse } from './UserSessionListPlannerResponse'
import type { UserSessionResponse } from './UserSessionResponse'
import { userSessionResponseFromPlannerResponse } from './UserSessionResponse'

// 本文件定义用户会话列表展示对象，并把当前会话标记收口在这里。
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
