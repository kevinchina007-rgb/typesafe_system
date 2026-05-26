import type { AuthSessionListResponse } from '@/microservices/auth/objects/AuthSessionListResponse'
import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'

import type { ManagerType } from '@/microservices/auth/objects/ManagerType'

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

function rememberManagerSession(sessionResponse: CurrentManagerSessionResponse & { sessionId?: string }): CurrentManagerSessionResponse {
  if (sessionResponse.sessionId) {
    window.localStorage.setItem(managerSessionStorageKey, sessionResponse.sessionId)
  }
  return sessionResponse
}

function forgetManagerSession() {
  window.localStorage.removeItem(managerSessionStorageKey)
}

export const loginManagerAuth = (payload: {
    managerType: ManagerType
    email: string
    password: string
  }): Promise<CurrentManagerSessionResponse> =>
    executeJsonApiRequest<CurrentManagerSessionResponse & { sessionId?: string }>('/ManagerLoginPlanner', 'POST', payload).then(rememberManagerSession)

export const logoutManagerAuth = (): Promise<{ status: string }> =>
    readManagerSessionId()
      ? executeJsonApiRequest<{ status: string }>('/ManagerLogoutPlanner', 'POST', { sessionId: readManagerSessionId() }).finally(forgetManagerSession)
      : Promise.resolve({ status: 'LoggedOut' })

export const getCurrentManagerSession = (): Promise<CurrentManagerSessionResponse> =>
    readManagerSessionId()
      ? executeJsonApiRequest<CurrentManagerSessionResponse & { sessionId?: string }>('/CurrentManagerPlanner', 'POST', { sessionId: readManagerSessionId() }).then(rememberManagerSession)
      : Promise.reject(new Error('manager_not_found|No active manager session'))

export const changeManagerPassword = (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
    executeJsonApiRequest('/ChangeManagerPasswordPlanner', 'POST', { ...payload, sessionId: readManagerSessionId() })

export const listManagerSessions = (): Promise<AuthSessionListResponse> =>
    executeJsonApiRequest('/ListManagerSessionsPlanner', 'POST', { sessionId: readManagerSessionId() })

export const logoutCurrentManagerSession = (): Promise<{ status: string }> =>
    logoutManagerAuth()

export const logoutOtherManagerSessions = (): Promise<{ revokedCount: number }> =>
    executeJsonApiRequest('/ManagerLogoutOtherSessionsPlanner', 'POST', { sessionId: readManagerSessionId() })
