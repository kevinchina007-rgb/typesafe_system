// 本文件定义 ManagerLogoutPlanner，负责 auth 模块的处理编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

function forgetManagerSession() {
  window.localStorage.removeItem(managerSessionStorageKey)
}

export const logoutManagerAuth = (): Promise<{ status: string }> =>
  readManagerSessionId()
    ? executeJsonApiRequest<{ status: string }>('/ManagerLogoutPlanner', 'POST', { sessionId: readManagerSessionId() }).finally(forgetManagerSession)
    : Promise.resolve({ status: 'LoggedOut' })

export const logoutCurrentManagerSession = (): Promise<{ status: string }> => logoutManagerAuth()
