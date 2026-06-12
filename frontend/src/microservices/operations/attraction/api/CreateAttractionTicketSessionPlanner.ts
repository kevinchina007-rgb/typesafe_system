// 本文件定义 CreateAttractionTicketSessionPlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { CreateAttractionTicketSessionPlannerRequest } from '@/microservices/attraction/objects/CreateAttractionTicketSessionPlannerRequest'

export const createAttractionTicketSession = (payload: CreateAttractionTicketSessionPlannerRequest): Promise<unknown> =>
  executeJsonApiRequest('/CreateAttractionTicketSessionPlanner', 'POST', payload)
