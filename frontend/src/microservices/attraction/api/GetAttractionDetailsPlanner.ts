// 本文件定义 GetAttractionDetailsPlanner，负责 attraction 模块的获取编排和接口入口。

import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { AttractionSearchQuery } from '@/microservices/attraction/objects/AttractionSearchQuery'
import { mapAttractionResponseFromBackend } from './AttractionDetailsResponseMapper'
import type { BackendAttractionResponse } from './AttractionResponseMapperSupport'

export const getAttraction = (attractionId: string, query?: Pick<AttractionSearchQuery, 'useDate'>): Promise<AttractionResponse> =>
  executeJsonApiRequest<BackendAttractionResponse>('/GetAttractionDetailsPlanner', 'POST', {
    attractionId,
    useDate: query?.useDate,
  }).then(response => mapAttractionResponseFromBackend(response, query?.useDate))
