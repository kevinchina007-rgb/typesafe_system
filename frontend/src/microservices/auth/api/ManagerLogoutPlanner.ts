import type { ManagerAuthStatusPlannerResponse } from '@/microservices/auth/objects/ManagerAuthStatusPlannerResponse'
import type { ManagerSessionPlannerRequest } from '@/microservices/auth/objects/ManagerSessionPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的管理员登出入口，仅编排 session 读取与缓存清理。
const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

function forgetManagerSession() {
  window.localStorage.removeItem(managerSessionStorageKey)
}

export const logoutManagerAuth = (): Promise<ManagerAuthStatusPlannerResponse> => {
  const sessionId = readManagerSessionId()
  return sessionId
    ? executeJsonApiRequest<ManagerAuthStatusPlannerResponse>('/ManagerLogoutPlanner', 'POST', {
        sessionId,
      } satisfies ManagerSessionPlannerRequest).finally(forgetManagerSession)
    : Promise.resolve({ status: 'LoggedOut', revokedCount: null })
}

export const logoutCurrentManagerSession = (): Promise<ManagerAuthStatusPlannerResponse> => logoutManagerAuth()
