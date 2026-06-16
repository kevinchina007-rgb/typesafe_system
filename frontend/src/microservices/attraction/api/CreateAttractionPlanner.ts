// 本文件定义 CreateAttractionPlanner，负责 operations 模块的创建编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { CreateAttractionPlannerRequest } from '@/microservices/attraction/objects/CreateAttractionPlannerRequest'

export const createAttraction = (payload: CreateAttractionPlannerRequest): Promise<AttractionResponse> =>
  executeJsonApiRequest<AttractionResponse>('/CreateAttractionPlanner', 'POST', payload)
