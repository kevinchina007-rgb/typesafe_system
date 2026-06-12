// 本文件定义 train 模块的 `TrainSeatAssignmentResponse`，作为响应数据并提供 JSON 编解码。

export type TrainSeatAssignmentResponse = {
  travelerId: string
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatPositionType: string
}
export const trainSeatAssignmentResponseFromJson = (json: string): TrainSeatAssignmentResponse =>
  JSON.parse(json) as TrainSeatAssignmentResponse

export const trainSeatAssignmentResponseToJson = (value: TrainSeatAssignmentResponse): string =>
  JSON.stringify(value)
