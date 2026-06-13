// 本文件定义 CurrentUserPlanner，负责 auth 模块的获取当前编排和接口入口。

import type { CurrentUserSessionResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

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

export const getCurrentUserSession = (): Promise<CurrentUserSessionResponse> =>
  readUserSessionId()
    ? executeJsonApiRequest<CurrentUserPlannerResponse>('/CurrentUserPlanner', 'POST', { sessionId: readUserSessionId() })
        .then(toCurrentUserSessionResponse)
        .catch(error => {
          if (isStaleUserSessionError(error)) {
            forgetUserSession()
            throw new Error('user_not_found|No active user session')
          }

          throw error
        })
    : Promise.reject(new Error('user_not_found|No active user session'))
