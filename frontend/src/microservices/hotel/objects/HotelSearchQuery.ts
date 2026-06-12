// 本文件定义 hotel 模块的 `HotelSearchQuery`，作为查询条件并提供 JSON 编解码。

export type HotelSearchPlannerRequest = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
}


export const hotelSearchQueryFromJson = (json: string): HotelSearchPlannerRequest =>
  JSON.parse(json) as HotelSearchPlannerRequest

export const hotelSearchQueryToJson = (value: HotelSearchPlannerRequest): string =>
  JSON.stringify(value)
