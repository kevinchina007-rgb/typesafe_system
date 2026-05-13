import type { UserResponse } from './UserResponse'

export type CurrentUserSessionResponse = {
  user: UserResponse
  expiresAt: string
}
