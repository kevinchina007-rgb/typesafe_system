// 本文件定义 ListManagedAttractionsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { AttractionListPlannerResponse } from '@/microservices/attraction/objects/AttractionListPlannerResponse'
import { mapAttractionListResponseFromBackend, type BackendAttractionListResponse } from '@/microservices/attraction/api/AttractionResponseMapper'
import type { ListManagedAttractionsPlannerRequest } from '@/microservices/attraction/objects/ListManagedAttractionsPlannerRequest'

export const listManagedAttractions = (payload: ListManagedAttractionsPlannerRequest): Promise<AttractionListPlannerResponse> =>
  executeJsonApiRequest<BackendAttractionListResponse>('/ListManagedAttractionsPlanner', 'POST', payload).then(response =>
    mapAttractionListResponseFromBackend(response),
  )
