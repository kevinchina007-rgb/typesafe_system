// 本文件定义 train 模块的 `TrainStopResponse`，作为响应数据并提供 JSON 编解码。

export type TrainStopResponse = {
  stopId: string
  stationCode: string
  stationName: string
  arrivalTime: string | null
  departureTime: string | null
  sequenceNo: number
}
export const trainStopResponseFromJson = (json: string): TrainStopResponse =>
  JSON.parse(json) as TrainStopResponse

export const trainStopResponseToJson = (value: TrainStopResponse): string =>
  JSON.stringify(value)
