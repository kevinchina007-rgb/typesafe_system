// 本文件定义 ChangeManagerPasswordPlanner，负责 auth 模块的修改编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

export const changeManagerPassword = (payload: { currentPassword: string; newPassword: string }): Promise<{ status: string }> =>
  executeJsonApiRequest('/ChangeManagerPasswordPlanner', 'POST', { ...payload, sessionId: readManagerSessionId() })
