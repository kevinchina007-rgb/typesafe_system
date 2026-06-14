// 本文件定义 train 模块的 `TrainSeatInventoryPlannerResponse`，用于表示座席库存并提供 JSON 编解码。

export type TrainSeatInventoryPlannerResponse = {
  inventoryId: string
  seatClass: string
  totalSeats: number
  saleableSeats: number
  status: string
}
export const trainSeatInventoryPlannerResponseFromJson = (json: string): TrainSeatInventoryPlannerResponse =>
  JSON.parse(json) as TrainSeatInventoryPlannerResponse

export const trainSeatInventoryPlannerResponseToJson = (value: TrainSeatInventoryPlannerResponse): string =>
  JSON.stringify(value)
