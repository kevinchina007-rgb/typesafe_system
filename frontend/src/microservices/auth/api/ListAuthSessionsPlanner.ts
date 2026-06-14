import type { UserSessionListResponse } from '@/microservices/auth/objects/UserSessionListResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const listUserSessions = (): Promise<UserSessionListResponse> =>
  executeJsonApiRequest('/ListAuthSessionsPlanner', 'POST', { sessionId: readUserSessionId() })
