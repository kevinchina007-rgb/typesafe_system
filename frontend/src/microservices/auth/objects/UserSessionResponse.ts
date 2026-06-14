export type UserSessionResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}

export const userSessionResponseFromJson = (json: string): UserSessionResponse =>
  JSON.parse(json) as UserSessionResponse

export const userSessionResponseToJson = (value: UserSessionResponse): string =>
  JSON.stringify(value)
