// 本文件定义 ListManagerSessionsPlanner，负责 auth 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

export const listManagerSessions = (): Promise<{ sessions: unknown[] }> =>
  executeJsonApiRequest('/ListManagerSessionsPlanner', 'POST', { sessionId: readManagerSessionId() })
