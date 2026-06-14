// 本文件把 attraction 列表响应映射为前端列表结构。

import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import { mapAttractionResponseInternal, type BackendAttractionListResponse } from './AttractionResponseMapperSupport'

export function mapAttractionListResponseFromBackend(response: BackendAttractionListResponse, useDate?: string): AttractionListResponse {
  return {
    attractions: response.attractions.map(attraction => mapAttractionResponseInternal(attraction, useDate)),
  }
}
