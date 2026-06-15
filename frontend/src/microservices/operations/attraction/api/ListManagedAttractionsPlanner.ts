// 本文件定义 ListManagedAttractionsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { AttractionListPlannerResponse } from '@/microservices/attraction/objects/AttractionListPlannerResponse'
import { mapAttractionListResponseFromBackend, type BackendAttractionListResponse } from '@/microservices/attraction/api/AttractionResponseMapper'

export const listManagedAttractions = (managerId: string): Promise<AttractionListPlannerResponse> =>
  executeJsonApiRequest<BackendAttractionListResponse>('/ListManagedAttractionsPlanner', 'POST', { managerId }).then(response =>
    mapAttractionListResponseFromBackend(response),
  )
