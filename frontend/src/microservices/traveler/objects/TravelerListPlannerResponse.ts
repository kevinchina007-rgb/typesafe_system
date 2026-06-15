// 本文件定义 traveler 模块的 `TravelerListPlannerResponse`，作为列表响应数据并提供 JSON 编解码。

import type { TravelerPlannerResponse } from './TravelerPlannerResponse'

export type TravelerListPlannerResponse = {
  travelers: TravelerPlannerResponse[]
}

export const travelerListPlannerResponseFromJson = (json: string): TravelerListPlannerResponse =>
  JSON.parse(json) as TravelerListPlannerResponse

export const travelerListPlannerResponseToJson = (value: TravelerListPlannerResponse): string =>
  JSON.stringify(value)
