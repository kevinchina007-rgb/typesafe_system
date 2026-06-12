// 本文件定义 LogoutOtherSessionsPlanner，负责 auth 模块的退出登录编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const logoutOtherUserSessions = (): Promise<{ revokedCount: number }> =>
  executeJsonApiRequest('/LogoutOtherSessionsPlanner', 'POST', { sessionId: readUserSessionId() })
