// 本文件定义 ListAuthSessionsPlanner，负责 auth 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const listUserSessions = (): Promise<{ sessions: unknown[] }> =>
  executeJsonApiRequest('/ListAuthSessionsPlanner', 'POST', { sessionId: readUserSessionId() })
