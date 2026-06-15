import type { UserSessionPlannerResponse } from './UserSessionPlannerResponse'

// 本文件保留为兼容层，补充“当前会话是否命中本地登录态”的前端展示字段。
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
