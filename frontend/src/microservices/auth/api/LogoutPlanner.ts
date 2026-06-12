// 本文件定义 LogoutPlanner，负责 auth 模块的退出登录编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

function forgetUserSession() {
  window.localStorage.removeItem(userSessionStorageKey)
}

export const logoutUser = (): Promise<{ status: string }> =>
  readUserSessionId()
    ? executeJsonApiRequest<{ status: string }>('/LogoutPlanner', 'POST', { sessionId: readUserSessionId() }).finally(forgetUserSession)
    : Promise.resolve({ status: 'LoggedOut' })

export const logoutCurrentUserSession = (): Promise<{ status: string }> => logoutUser()
