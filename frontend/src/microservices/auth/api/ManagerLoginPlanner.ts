// 本文件定义 ManagerLoginPlanner，负责 auth 模块的处理编排和接口入口。

import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import type { ManagerType } from '@/microservices/auth/objects/ManagerType'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function rememberManagerSession(sessionResponse: CurrentManagerSessionResponse & { sessionId?: string }): CurrentManagerSessionResponse {
  if (sessionResponse.sessionId) {
    window.localStorage.setItem(managerSessionStorageKey, sessionResponse.sessionId)
  }
  return sessionResponse
}

export const loginManagerAuth = (payload: {
  managerType: ManagerType
  email: string
  password: string
}): Promise<CurrentManagerSessionResponse> =>
  executeJsonApiRequest<CurrentManagerSessionResponse & { sessionId?: string }>('/ManagerLoginPlanner', 'POST', payload).then(rememberManagerSession)
