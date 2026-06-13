// 本文件定义 CurrentManagerPlanner，负责 auth 模块的获取当前编排和接口入口。

import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

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

function rememberManagerSession(sessionResponse: CurrentManagerSessionResponse & { sessionId?: string }): CurrentManagerSessionResponse {
  if (sessionResponse.sessionId) {
    window.localStorage.setItem(managerSessionStorageKey, sessionResponse.sessionId)
  }
  return sessionResponse
}

function isStaleManagerSessionError(error: unknown): boolean {
  if (!(error instanceof Error)) {
    return false
  }

  const normalizedMessage = error.message.toLowerCase()
  return staleManagerSessionErrorFragments.some(fragment => normalizedMessage.includes(fragment))
}

export const getCurrentManagerSession = (): Promise<CurrentManagerSessionResponse> =>
  readManagerSessionId()
    ? executeJsonApiRequest<CurrentManagerSessionResponse & { sessionId?: string }>('/CurrentManagerPlanner', 'POST', { sessionId: readManagerSessionId() })
        .then(rememberManagerSession)
        .catch(error => {
          if (isStaleManagerSessionError(error)) {
            forgetManagerSession()
            throw new Error('manager_not_found|No active manager session')
          }

          throw error
        })
    : Promise.reject(new Error('manager_not_found|No active manager session'))
