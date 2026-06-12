// 本文件定义 hotel 模块的 `HotelListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { HotelPlannerResponse } from './HotelPlannerResponse'

export type HotelListPlannerResponse = {
  hotels: HotelPlannerResponse[]
}


export const hotelListResponseFromJson = (json: string): HotelListPlannerResponse =>
  JSON.parse(json) as HotelListPlannerResponse

export const hotelListResponseToJson = (value: HotelListPlannerResponse): string =>
  JSON.stringify(value)
