// 本文件定义 hotel 模块的 `RoomTypeSummaryResponse`，作为摘要响应数据并提供 JSON 编解码。

export type RoomTypeSummaryResponse = {
  roomTypeId: string
  roomTypeName: string
  capacity: number
  bedType: string
  nightlyPrice: string
  basePrice: string
  currency: string
  imageUrl: string | null
  availableRooms: number
  availableRoomsForRequestedStay: number | null
  isBookableForRequestedStay: boolean
}
export const roomTypeSummaryResponseFromJson = (json: string): RoomTypeSummaryResponse =>
  JSON.parse(json) as RoomTypeSummaryResponse

export const roomTypeSummaryResponseToJson = (value: RoomTypeSummaryResponse): string =>
  JSON.stringify(value)
