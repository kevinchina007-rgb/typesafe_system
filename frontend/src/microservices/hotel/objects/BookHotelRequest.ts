// 本文件定义 hotel 模块的 `BookHotelRequest`，作为请求参数并提供 JSON 编解码。

export type BookHotelPlannerRequest = {
  userId: string
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}


export const bookHotelRequestFromJson = (json: string): BookHotelPlannerRequest =>
  JSON.parse(json) as BookHotelPlannerRequest

export const bookHotelRequestToJson = (value: BookHotelPlannerRequest): string =>
  JSON.stringify(value)
