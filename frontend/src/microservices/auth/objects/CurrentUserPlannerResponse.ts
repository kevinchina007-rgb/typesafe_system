// 本文件定义后端 CurrentUserPlanner 对应的当前用户会话响应结构，并提供 JSON 编解码。

export type CurrentUserPlannerResponse = {
  sessionId: string
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
  membershipLevel: string
  points: number
  expiresAt: string
}

export const currentUserPlannerResponseFromJson = (json: string): CurrentUserPlannerResponse =>
  JSON.parse(json) as CurrentUserPlannerResponse

export const currentUserPlannerResponseToJson = (value: CurrentUserPlannerResponse): string =>
  JSON.stringify(value)
