import type { UserSessionListPlannerResponse } from '@/microservices/auth/objects/UserSessionListPlannerResponse'
import type { UserSessionListResponse } from '@/microservices/auth/objects/UserSessionListResponse'
import { userSessionListResponseFromPlannerResponse } from '@/microservices/auth/objects/UserSessionListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的用户会话列表入口，仅编排 session 读取与兼容转换。
const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const listUserSessions = (): Promise<UserSessionListResponse> => {
  const sessionId = readUserSessionId()
  return executeJsonApiRequest<UserSessionListPlannerResponse>('/ListAuthSessionsPlanner', 'POST', { sessionId }).then(response =>
    userSessionListResponseFromPlannerResponse(response, sessionId),
  )
}
