// 本文件定义 train 模块的 `TrainSeatResponse`，作为响应数据并提供 JSON 编解码。

export type TrainSeatResponse = {
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatClass: string
  seatPositionType: string
  status: string
}
export const trainSeatResponseFromJson = (json: string): TrainSeatResponse =>
  JSON.parse(json) as TrainSeatResponse

export const trainSeatResponseToJson = (value: TrainSeatResponse): string =>
  JSON.stringify(value)
