// 本文件定义 ManagerLogoutOtherSessionsPlanner，负责 auth 模块的处理编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

export const logoutOtherManagerSessions = (): Promise<{ revokedCount: number }> =>
  executeJsonApiRequest('/ManagerLogoutOtherSessionsPlanner', 'POST', { sessionId: readManagerSessionId() })
