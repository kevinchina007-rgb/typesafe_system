import type { AuthSessionResponse } from './AuthSessionResponse'

// 会话列表接口返回的数据结构。
export type AuthSessionListResponse = {
  sessions: AuthSessionResponse[]
}

// 把会话列表 JSON 解析成对象。
export const authSessionListResponseFromJson = (json: string): AuthSessionListResponse =>
  JSON.parse(json) as AuthSessionListResponse

// 把会话列表对象序列化成 JSON。
export const authSessionListResponseToJson = (value: AuthSessionListResponse): string =>
  JSON.stringify(value)
