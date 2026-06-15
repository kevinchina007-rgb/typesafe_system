import type { CurrentManagerPlannerResponse } from '@/microservices/auth/objects/CurrentManagerPlannerResponse'
import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import { currentManagerSessionResponseFromPlannerResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import type { ManagerLoginPlannerRequest } from '@/microservices/auth/objects/ManagerLoginPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的管理员登录入口，仅编排请求转换与本地会话缓存。
const managerSessionStorageKey = 'flypig.managerSessionId'

function rememberManagerSession(sessionResponse: CurrentManagerPlannerResponse & { sessionId?: string }): CurrentManagerSessionResponse {
  if (sessionResponse.sessionId) {
    window.localStorage.setItem(managerSessionStorageKey, sessionResponse.sessionId)
  }
  return currentManagerSessionResponseFromPlannerResponse(sessionResponse)
}

export const loginManagerAuth = (payload: ManagerLoginPlannerRequest): Promise<CurrentManagerSessionResponse> =>
  executeJsonApiRequest<CurrentManagerPlannerResponse & { sessionId?: string }>('/ManagerLoginPlanner', 'POST', payload).then(rememberManagerSession)
