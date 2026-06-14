// 本文件定义 train 模块的 `TrainSeatPlannerResponse`，用于表示单个座位信息并提供 JSON 编解码。

export type TrainSeatPlannerResponse = {
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatClass: string
  seatPositionType: string
  status: string
}
export const trainSeatPlannerResponseFromJson = (json: string): TrainSeatPlannerResponse =>
  JSON.parse(json) as TrainSeatPlannerResponse

export const trainSeatPlannerResponseToJson = (value: TrainSeatPlannerResponse): string =>
  JSON.stringify(value)
