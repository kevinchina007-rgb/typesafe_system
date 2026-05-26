export type AuthSessionResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}
export const authSessionResponseFromJson = (json: string): AuthSessionResponse =>
  JSON.parse(json) as AuthSessionResponse

export const authSessionResponseToJson = (value: AuthSessionResponse): string =>
  JSON.stringify(value)
