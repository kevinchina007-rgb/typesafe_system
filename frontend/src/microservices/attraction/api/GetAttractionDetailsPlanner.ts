// 本文件定义 GetAttractionDetailsPlanner，负责 attraction 模块的获取编排和接口入口。

import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { GetAttractionDetailsPlannerRequest } from '@/microservices/attraction/objects/GetAttractionDetailsPlannerRequest'
import { mapAttractionResponseFromBackend, type BackendAttractionResponse } from './AttractionResponseMapper'

export const getAttraction = (attractionId: string, query?: Pick<GetAttractionDetailsPlannerRequest, 'useDate'>): Promise<AttractionResponse> =>
  executeJsonApiRequest<BackendAttractionResponse>('/GetAttractionDetailsPlanner', 'POST', {
    attractionId,
    useDate: query?.useDate,
  }).then(response => mapAttractionResponseFromBackend(response, query?.useDate))
