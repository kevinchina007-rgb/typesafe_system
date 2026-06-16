// 本文件定义 CreateAttractionTicketRulePlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateAttractionTicketRulePlannerRequest } from '@/microservices/operations/attraction/objects/CreateAttractionTicketRulePlannerRequest'

export const createAttractionTicketRule = (payload: CreateAttractionTicketRulePlannerRequest): Promise<void> =>
  executeJsonApiRequest<void>('/CreateAttractionTicketRulePlanner', 'POST', payload)

