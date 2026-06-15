import type { AuthStatusPlannerResponse } from '@/microservices/auth/objects/AuthStatusPlannerResponse'
import type { SessionPlannerRequest } from '@/microservices/auth/objects/SessionPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的用户登出入口，仅编排 session 读取与缓存清理。
const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

function forgetUserSession() {
  window.localStorage.removeItem(userSessionStorageKey)
}

export const logoutUser = (): Promise<AuthStatusPlannerResponse> => {
  const sessionId = readUserSessionId()
  return sessionId
    ? executeJsonApiRequest<AuthStatusPlannerResponse>('/LogoutPlanner', 'POST', { sessionId } satisfies SessionPlannerRequest).finally(forgetUserSession)
    : Promise.resolve({ status: 'LoggedOut', revokedCount: null })
}

export const logoutCurrentUserSession = (): Promise<AuthStatusPlannerResponse> => logoutUser()
