import type { AuthStatusPlannerResponse } from '@/microservices/auth/objects/AuthStatusPlannerResponse'
import type { ChangePasswordPlannerRequest } from '@/microservices/auth/objects/ChangePasswordPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的用户改密入口，仅编排 session 读取与请求转发。
const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export const changeUserPassword = (payload: ChangePasswordPlannerRequest): Promise<AuthStatusPlannerResponse> =>
  executeJsonApiRequest<AuthStatusPlannerResponse>('/ChangePasswordPlanner', 'POST', { ...payload, sessionId: readUserSessionId() })
