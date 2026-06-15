// 本文件定义后端 UserSessionPlannerResponse 对应的用户会话条目结构，并提供 JSON 编解码。

export type UserSessionPlannerResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
}

export const userSessionPlannerResponseFromJson = (json: string): UserSessionPlannerResponse =>
  JSON.parse(json) as UserSessionPlannerResponse

export const userSessionPlannerResponseToJson = (value: UserSessionPlannerResponse): string =>
  JSON.stringify(value)
