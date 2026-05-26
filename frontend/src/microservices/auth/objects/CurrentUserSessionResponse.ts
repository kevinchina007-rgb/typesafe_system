import type { UserResponse } from './UserResponse'

export type CurrentUserSessionResponse = {
  user: UserResponse
  expiresAt: string
}
export const currentUserSessionResponseFromJson = (json: string): CurrentUserSessionResponse =>
  JSON.parse(json) as CurrentUserSessionResponse

export const currentUserSessionResponseToJson = (value: CurrentUserSessionResponse): string =>
  JSON.stringify(value)
