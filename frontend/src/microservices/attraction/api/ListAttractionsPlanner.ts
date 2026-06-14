// 本文件定义 ListAttractionsPlanner，负责 attraction 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { AttractionListPlannerResponse } from '@/microservices/attraction/objects/AttractionListPlannerResponse'
import type { ListAttractionsPlannerRequest } from '@/microservices/attraction/objects/ListAttractionsPlannerRequest'
import { mapAttractionListResponseFromBackend, type BackendAttractionListResponse } from './AttractionResponseMapper'

export const listAttractions = (query?: ListAttractionsPlannerRequest): Promise<AttractionListPlannerResponse> =>
  executeJsonApiRequest<BackendAttractionListResponse>('/ListAttractionsPlanner', 'POST', {
    city: query?.city,
    keyword: query?.keyword,
    useDate: query?.useDate,
  }).then(response => mapAttractionListResponseFromBackend(response, query?.useDate))
