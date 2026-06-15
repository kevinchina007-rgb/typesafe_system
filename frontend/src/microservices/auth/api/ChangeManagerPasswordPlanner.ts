import type { ManagerAuthStatusPlannerResponse } from '@/microservices/auth/objects/ManagerAuthStatusPlannerResponse'
import type { ManagerChangePasswordPlannerRequest } from '@/microservices/auth/objects/ManagerChangePasswordPlannerRequest'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

// 本文件负责 auth 模块的管理员改密入口，仅编排 session 读取与请求转发。
const managerSessionStorageKey = 'flypig.managerSessionId'

function readManagerSessionId(): string | null {
  return window.localStorage.getItem(managerSessionStorageKey)
}

export const changeManagerPassword = (payload: ManagerChangePasswordPlannerRequest): Promise<ManagerAuthStatusPlannerResponse> =>
  executeJsonApiRequest<ManagerAuthStatusPlannerResponse>('/ChangeManagerPasswordPlanner', 'POST', {
    ...payload,
    sessionId: readManagerSessionId(),
  })
