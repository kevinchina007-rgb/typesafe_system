// 本文件定义 CreateAttractionTicketTypePlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { CreateAttractionTicketTypePlannerRequest } from '@/microservices/attraction/objects/CreateAttractionTicketTypePlannerRequest'

export const createAttractionTicketType = (payload: CreateAttractionTicketTypePlannerRequest): Promise<void> =>
  executeJsonApiRequest<void>('/CreateAttractionTicketTypePlanner', 'POST', payload)
