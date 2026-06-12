// 单个用户或管理员会话的展示结构。
export type AuthSessionResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}

// 把会话 JSON 解析成对象。
export const authSessionResponseFromJson = (json: string): AuthSessionResponse =>
  JSON.parse(json) as AuthSessionResponse

// 把会话对象序列化成 JSON。
export const authSessionResponseToJson = (value: AuthSessionResponse): string =>
  JSON.stringify(value)
