// 本文件定义 tour-group 模块的 `TourGroupListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { TourGroupSummaryResponse } from './TourGroupSummaryResponse'

export type TourGroupListResponse = {
  groups: TourGroupSummaryResponse[]
}
export const tourGroupListResponseFromJson = (json: string): TourGroupListResponse =>
  JSON.parse(json) as TourGroupListResponse

export const tourGroupListResponseToJson = (value: TourGroupListResponse): string =>
  JSON.stringify(value)
