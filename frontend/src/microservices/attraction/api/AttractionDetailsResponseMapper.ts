// 本文件把 attraction 详情响应映射为前端详情结构。

import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import { mapAttractionResponseInternal, type BackendAttractionResponse } from './AttractionResponseMapperSupport'

export function mapAttractionResponseFromBackend(response: BackendAttractionResponse, useDate?: string): AttractionResponse {
  return mapAttractionResponseInternal(response, useDate)
}
