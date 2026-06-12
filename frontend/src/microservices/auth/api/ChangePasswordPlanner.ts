// 本文件定义 ChangePasswordPlanner，负责 auth 模块的修改编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const changeUserPassword = (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
  executeJsonApiRequest('/ChangePasswordPlanner', 'POST', { ...payload, sessionId: readUserSessionId() })
