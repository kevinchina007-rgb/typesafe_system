export type AuthSessionResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}
