import type { ManagerSessionListPlannerResponse } from '@/microservices/auth/objects/ManagerSessionListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的管理员会话列表入口，仅编排 session 读取。
const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

export const listManagerSessions = (): Promise<ManagerSessionListPlannerResponse> => {
  const sessionId = readManagerSessionId()
  return executeJsonApiRequest<ManagerSessionListPlannerResponse>('/ListManagerSessionsPlanner', 'POST', { sessionId })
}
