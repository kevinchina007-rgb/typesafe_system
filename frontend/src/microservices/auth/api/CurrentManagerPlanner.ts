import type { CurrentManagerPlannerResponse } from '@/microservices/auth/objects/CurrentManagerPlannerResponse'
import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import { currentManagerSessionResponseFromPlannerResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的当前管理员会话入口，仅编排 session 读取与容错。
const managerSessionStorageKey = 'flypig.managerSessionId'

const staleManagerSessionErrorFragments = [
  'signed-in manager session is required',
  'session was not found',
  'has expired',
  'has been revoked',
  'current session does not match the requested actor',
]

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

function forgetManagerSession() {
  window.localStorage.removeItem(managerSessionStorageKey)
}

function rememberManagerSession(sessionResponse: CurrentManagerPlannerResponse & { sessionId?: string }): CurrentManagerSessionResponse {
  if (sessionResponse.sessionId) {
    window.localStorage.setItem(managerSessionStorageKey, sessionResponse.sessionId)
  }
  return currentManagerSessionResponseFromPlannerResponse(sessionResponse)
}

function isStaleManagerSessionError(error: unknown): boolean {
  if (!(error instanceof Error)) {
    return false
  }

  const normalizedMessage = error.message.toLowerCase()
  return staleManagerSessionErrorFragments.some(fragment => normalizedMessage.includes(fragment))
}

export const getCurrentManagerSession = (): Promise<CurrentManagerSessionResponse> => {
  const sessionId = readManagerSessionId()
  return sessionId
    ? executeJsonApiRequest<CurrentManagerPlannerResponse & { sessionId?: string }>('/CurrentManagerPlanner', 'POST', { sessionId })
        .then(rememberManagerSession)
        .catch(error => {
          if (isStaleManagerSessionError(error)) {
            forgetManagerSession()
            throw new Error('manager_not_found|No active manager session')
          }

          throw error
        })
    : Promise.reject(new Error('manager_not_found|No active manager session'))
}
