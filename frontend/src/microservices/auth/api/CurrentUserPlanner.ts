import type { CurrentUserPlannerResponse } from '@/microservices/auth/objects/CurrentUserPlannerResponse'
import type { CurrentUserSessionResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'
import { currentUserSessionResponseFromPlannerResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的当前用户会话入口，仅编排 session 读取与容错。
const userSessionStorageKey = 'flypig.userSessionId'

const staleUserSessionErrorFragments = [
  'signed-in user session is required',
  'session was not found',
  'has expired',
  'has been revoked',
  'current session does not match the requested actor',
]

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

function forgetUserSession() {
  window.localStorage.removeItem(userSessionStorageKey)
}

function rememberUserSession(sessionId: string) {
  window.localStorage.setItem(userSessionStorageKey, sessionId)
}

function isStaleUserSessionError(error: unknown): boolean {
  if (!(error instanceof Error)) {
    return false
  }

  const normalizedMessage = error.message.toLowerCase()
  return staleUserSessionErrorFragments.some(fragment => normalizedMessage.includes(fragment))
}

function toCurrentUserSessionResponse(plannerResponse: CurrentUserPlannerResponse): CurrentUserSessionResponse {
  rememberUserSession(plannerResponse.sessionId)
  return currentUserSessionResponseFromPlannerResponse(plannerResponse)
}

export const getCurrentUserSession = (): Promise<CurrentUserSessionResponse> => {
  const sessionId = readUserSessionId()
  return sessionId
    ? executeJsonApiRequest<CurrentUserPlannerResponse>('/CurrentUserPlanner', 'POST', { sessionId })
        .then(toCurrentUserSessionResponse)
        .catch(error => {
          if (isStaleUserSessionError(error)) {
            forgetUserSession()
            throw new Error('user_not_found|No active user session')
          }

          throw error
        })
    : Promise.reject(new Error('user_not_found|No active user session'))
}
