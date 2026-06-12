// 本文件定义 traveler 模块的 `TravelerListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { TravelerResponse } from './TravelerResponse'

export type TravelerListResponse = {
  travelers: TravelerResponse[]
}
export const travelerListResponseFromJson = (json: string): TravelerListResponse =>
  JSON.parse(json) as TravelerListResponse

export const travelerListResponseToJson = (value: TravelerListResponse): string =>
  JSON.stringify(value)
