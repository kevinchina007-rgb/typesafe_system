import type { ManagerAuthStatusPlannerResponse } from '@/microservices/auth/objects/ManagerAuthStatusPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的管理员“登出其他会话”入口，仅编排 session 读取。
const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

export const logoutOtherManagerSessions = (): Promise<ManagerAuthStatusPlannerResponse> =>
  executeJsonApiRequest<ManagerAuthStatusPlannerResponse>('/ManagerLogoutOtherSessionsPlanner', 'POST', {
    sessionId: readManagerSessionId(),
  })
