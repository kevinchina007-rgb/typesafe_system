// 本文件定义 train 模块的 `TrainSeatAssignmentPlannerResponse`，用于描述乘客与座位的分配结果并提供 JSON 编解码。

export type TrainSeatAssignmentPlannerResponse = {
  travelerId: string
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatPositionType: string
}

export const trainSeatAssignmentPlannerResponseFromJson = (json: string): TrainSeatAssignmentPlannerResponse =>
  JSON.parse(json) as TrainSeatAssignmentPlannerResponse

export const trainSeatAssignmentPlannerResponseToJson = (value: TrainSeatAssignmentPlannerResponse): string =>
  JSON.stringify(value)
