import type { AuthSessionResponse } from './AuthSessionResponse'

export type AuthSessionListResponse = {
  sessions: AuthSessionResponse[]
}
export const authSessionListResponseFromJson = (json: string): AuthSessionListResponse =>
  JSON.parse(json) as AuthSessionListResponse

export const authSessionListResponseToJson = (value: AuthSessionListResponse): string =>
  JSON.stringify(value)
