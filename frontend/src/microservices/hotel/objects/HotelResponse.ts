// 本文件定义 hotel 模块的 `HotelResponse`，作为响应数据并提供 JSON 编解码。

import type { RoomTypeSummaryResponse } from './RoomTypeSummaryResponse'

export type HotelPlannerResponse = {
  hotelId: string
  hotelName: string
  location: string
  status: string
  createdAt: string
  roomTypes: RoomTypeSummaryResponse[]
}


export const hotelResponseFromJson = (json: string): HotelPlannerResponse =>
  JSON.parse(json) as HotelPlannerResponse

export const hotelResponseToJson = (value: HotelPlannerResponse): string =>
  JSON.stringify(value)
