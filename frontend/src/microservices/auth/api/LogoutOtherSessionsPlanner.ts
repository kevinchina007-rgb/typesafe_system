import type { AuthStatusPlannerResponse } from '@/microservices/auth/objects/AuthStatusPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的“登出其他会话”入口，仅编排 session 读取。
const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const logoutOtherUserSessions = (): Promise<AuthStatusPlannerResponse> =>
  executeJsonApiRequest<AuthStatusPlannerResponse>('/LogoutOtherSessionsPlanner', 'POST', {
    sessionId: readUserSessionId(),
  })
