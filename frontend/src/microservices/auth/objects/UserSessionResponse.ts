import type { UserSessionPlannerResponse } from './UserSessionPlannerResponse'

// 本文件定义用户会话展示对象，并附带当前会话标记。
export type UserSessionResponse = UserSessionPlannerResponse & {
  isCurrent: boolean
}

export function userSessionResponseFromPlannerResponse(
  plannerResponse: UserSessionPlannerResponse,
  isCurrent: boolean,
): UserSessionResponse {
  return {
    ...plannerResponse,
    isCurrent,
  }
}

export const userSessionResponseFromJson = (json: string): UserSessionResponse =>
  JSON.parse(json) as UserSessionResponse

export const userSessionResponseToJson = (value: UserSessionResponse): string =>
  JSON.stringify(value)
