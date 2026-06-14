import type { UserSessionResponse } from './UserSessionResponse'

export type UserSessionListResponse = {
  sessions: UserSessionResponse[]
}

export const userSessionListResponseFromJson = (json: string): UserSessionListResponse =>
  JSON.parse(json) as UserSessionListResponse

export const userSessionListResponseToJson = (value: UserSessionListResponse): string =>
  JSON.stringify(value)
