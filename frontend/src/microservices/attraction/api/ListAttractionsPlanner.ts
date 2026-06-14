// 本文件定义 ListAttractionsPlanner，负责 attraction 模块的列表查询编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import type { AttractionSearchQuery } from '@/microservices/attraction/objects/AttractionSearchQuery'
import { mapAttractionListResponseFromBackend } from './AttractionListResponseMapper'
import type { BackendAttractionListResponse } from './AttractionResponseMapperSupport'

export const listAttractions = (query?: AttractionSearchQuery): Promise<AttractionListResponse> =>
  executeJsonApiRequest<BackendAttractionListResponse>('/ListAttractionsPlanner', 'POST', {
    city: query?.city,
    keyword: query?.keyword,
    useDate: query?.useDate,
  }).then(response => mapAttractionListResponseFromBackend(response, query?.useDate))
