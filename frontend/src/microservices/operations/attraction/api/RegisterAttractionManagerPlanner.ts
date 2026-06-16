// 本文件定义 RegisterAttractionManagerPlanner，负责 operations 景点管理端的注册编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { RegisterAttractionManagerPlannerRequest } from '@/microservices/operations/attraction/objects/RegisterAttractionManagerPlannerRequest'
import type { AttractionManagerSessionPlannerResponse } from '@/microservices/operations/attraction/objects/AttractionManagerSessionPlannerResponse'

export const registerAttractionManager = (payload: RegisterAttractionManagerPlannerRequest): Promise<AttractionManagerSessionPlannerResponse> =>
  executeJsonApiRequest<AttractionManagerSessionPlannerResponse>('/RegisterAttractionManagerPlanner', 'POST', payload)


