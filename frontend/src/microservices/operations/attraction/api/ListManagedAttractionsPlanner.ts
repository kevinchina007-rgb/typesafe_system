// 本文件定义 ListManagedAttractionsPlanner，负责 operations 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import { mapAttractionListResponseFromBackend } from '@/microservices/attraction/api/AttractionListResponseMapper'
import type { BackendAttractionListResponse } from '@/microservices/attraction/api/AttractionResponseMapperSupport'

export const listManagedAttractions = (managerId: string): Promise<AttractionListResponse> =>
  executeJsonApiRequest<BackendAttractionListResponse>('/ListManagedAttractionsPlanner', 'POST', { managerId }).then(response =>
    mapAttractionListResponseFromBackend(response),
  )
