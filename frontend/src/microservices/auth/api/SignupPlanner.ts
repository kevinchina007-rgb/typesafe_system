// 本文件定义 SignupPlanner，负责 auth 模块的处理编排和接口入口。

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

function rememberUserSession(sessionId: string) {
  window.localStorage.setItem(userSessionStorageKey, sessionId)
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

export const signupUser = (payload: {
  email: string
  nickname: string
  phone: string
  password: string
}): Promise<CurrentUserSessionResponse> =>
  executeJsonApiRequest<CurrentUserPlannerResponse>('/SignupPlanner', 'POST', payload).then(toCurrentUserSessionResponse)
