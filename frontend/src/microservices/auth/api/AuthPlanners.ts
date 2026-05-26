import type { AuthSessionListResponse } from '@/microservices/auth/objects/AuthSessionListResponse'

import type { CurrentUserSessionResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'

import type { HealthResponse } from '@/microservices/common/objects/HealthResponse'
import { executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

type CurrentUserPlannerResponse = {
  sessionId: string
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
  membershipLevel: string
  points: number
  expiresAt: string
}

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

function rememberUserSession(sessionId: string) {
  window.localStorage.setItem(userSessionStorageKey, sessionId)
}

function forgetUserSession() {
  window.localStorage.removeItem(userSessionStorageKey)
}

function toCurrentUserSessionResponse(plannerResponse: CurrentUserPlannerResponse): CurrentUserSessionResponse {
  rememberUserSession(plannerResponse.sessionId)
  return {
    user: {
      userId: plannerResponse.userId,
      email: plannerResponse.email,
      nickname: plannerResponse.nickname,
      phone: plannerResponse.phone,
      avatarUrl: plannerResponse.avatarUrl,
      status: 'Active',
      membershipLevel: plannerResponse.membershipLevel,
      points: plannerResponse.points,
      defaultTravelerProfileId: null,
      createdAt: new Date().toISOString(),
    },
    expiresAt: plannerResponse.expiresAt,
  }
}

export const getHealth = (): Promise<HealthResponse> => executeApiRequest('/health')

export const signupUser = (payload: {
    email: string
    nickname: string
    phone: string
    password: string
  }): Promise<CurrentUserSessionResponse> =>
    executeJsonApiRequest<CurrentUserPlannerResponse>('/SignupPlanner', 'POST', payload).then(toCurrentUserSessionResponse)

export const loginUserWithPassword = (payload: {
    email: string
    password: string
  }): Promise<CurrentUserSessionResponse> =>
    executeJsonApiRequest<CurrentUserPlannerResponse>('/LoginPlanner', 'POST', payload).then(toCurrentUserSessionResponse)

export const logoutUser = (): Promise<{ status: string }> =>
    readUserSessionId()
      ? executeJsonApiRequest<{ status: string }>('/LogoutPlanner', 'POST', { sessionId: readUserSessionId() }).finally(forgetUserSession)
      : Promise.resolve({ status: 'LoggedOut' })

export const getCurrentUserSession = (): Promise<CurrentUserSessionResponse> =>
    readUserSessionId()
      ? executeJsonApiRequest<CurrentUserPlannerResponse>('/CurrentUserPlanner', 'POST', { sessionId: readUserSessionId() }).then(toCurrentUserSessionResponse)
      : Promise.reject(new Error('user_not_found|No active user session'))

export const changeUserPassword = (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
    executeJsonApiRequest('/ChangePasswordPlanner', 'POST', { ...payload, sessionId: readUserSessionId() })

export const listUserSessions = (): Promise<AuthSessionListResponse> =>
    executeJsonApiRequest('/ListAuthSessionsPlanner', 'POST', { sessionId: readUserSessionId() })

export const logoutCurrentUserSession = (): Promise<{ status: string }> =>
    logoutUser()

export const logoutOtherUserSessions = (): Promise<{ revokedCount: number }> =>
    executeJsonApiRequest('/LogoutOtherSessionsPlanner', 'POST', { sessionId: readUserSessionId() })
