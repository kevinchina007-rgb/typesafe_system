// 本文件定义 train 模块的 `TrainSeatInventoryResponse`，作为响应数据并提供 JSON 编解码。

export type TrainSeatInventoryResponse = {
  inventoryId: string
  seatClass: string
  totalSeats: number
  saleableSeats: number
  status: string
}
export const trainSeatInventoryResponseFromJson = (json: string): TrainSeatInventoryResponse =>
  JSON.parse(json) as TrainSeatInventoryResponse

export const trainSeatInventoryResponseToJson = (value: TrainSeatInventoryResponse): string =>
  JSON.stringify(value)
