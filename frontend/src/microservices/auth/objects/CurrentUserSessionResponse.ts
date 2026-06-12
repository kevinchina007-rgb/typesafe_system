import type { UserResponse } from './UserResponse'

// 当前用户登录后返回的会话结构。
export type CurrentUserSessionResponse = {
  user: UserResponse
  expiresAt: string
}

// 把当前用户会话 JSON 解析成对象。
export const currentUserSessionResponseFromJson = (json: string): CurrentUserSessionResponse =>
  JSON.parse(json) as CurrentUserSessionResponse

// 把当前用户会话对象序列化成 JSON。
export const currentUserSessionResponseToJson = (value: CurrentUserSessionResponse): string =>
  JSON.stringify(value)
