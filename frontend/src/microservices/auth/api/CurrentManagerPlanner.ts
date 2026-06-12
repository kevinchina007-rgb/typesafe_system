// 本文件定义 CurrentManagerPlanner，负责 auth 模块的获取当前编排和接口入口。

import type { CurrentManagerSessionResponse } from '@/microservices/auth/objects/CurrentManagerSessionResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

function rememberManagerSession(sessionResponse: CurrentManagerSessionResponse & { sessionId?: string }): CurrentManagerSessionResponse {
  if (sessionResponse.sessionId) {
    window.localStorage.setItem(managerSessionStorageKey, sessionResponse.sessionId)
  }
  return sessionResponse
}

export const getCurrentManagerSession = (): Promise<CurrentManagerSessionResponse> =>
  readManagerSessionId()
    ? executeJsonApiRequest<CurrentManagerSessionResponse & { sessionId?: string }>('/CurrentManagerPlanner', 'POST', { sessionId: readManagerSessionId() }).then(rememberManagerSession)
    : Promise.reject(new Error('manager_not_found|No active manager session'))
