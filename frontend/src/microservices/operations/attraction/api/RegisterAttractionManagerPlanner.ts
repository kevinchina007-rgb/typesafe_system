// 本文件定义 RegisterAttractionManagerPlanner，负责 operations 模块的注册编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { ManagerSessionResponse } from '@/microservices/auth/objects/ManagerSessionResponse'
import type { RegisterAttractionManagerPlannerRequest } from '@/microservices/operations/objects/RegisterAttractionManagerPlannerRequest'

export const registerAttractionManager = (payload: RegisterAttractionManagerPlannerRequest): Promise<ManagerSessionResponse> =>
  executeJsonApiRequest('/RegisterAttractionManagerPlanner', 'POST', payload)
