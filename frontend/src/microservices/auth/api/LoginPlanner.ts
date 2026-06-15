import type { CurrentUserPlannerResponse } from '@/microservices/auth/objects/CurrentUserPlannerResponse'
import type { CurrentUserSessionResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'
import { currentUserSessionResponseFromPlannerResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'
import type { LoginPlannerRequest } from '@/microservices/auth/objects/LoginPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的用户登录入口，仅编排请求转换与本地会话缓存。
const userSessionStorageKey = 'flypig.userSessionId'

function rememberUserSession(sessionId: string) {
  window.localStorage.setItem(userSessionStorageKey, sessionId)
}

function toCurrentUserSessionResponse(plannerResponse: CurrentUserPlannerResponse): CurrentUserSessionResponse {
  rememberUserSession(plannerResponse.sessionId)
  return currentUserSessionResponseFromPlannerResponse(plannerResponse)
}

export const loginUserWithPassword = (payload: LoginPlannerRequest): Promise<CurrentUserSessionResponse> =>
  executeJsonApiRequest<CurrentUserPlannerResponse>('/LoginPlanner', 'POST', payload).then(toCurrentUserSessionResponse)
