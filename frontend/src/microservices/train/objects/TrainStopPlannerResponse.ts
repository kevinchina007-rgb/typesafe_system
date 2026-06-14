// 本文件定义 train 模块的 `TrainStopPlannerResponse`，用于表示火车经停站并提供 JSON 编解码。

export type TrainStopPlannerResponse = {
  stopId: string
  stationCode: string
  stationName: string
  arrivalTime: string | null
  departureTime: string | null
  sequenceNo: number
}
export const trainStopPlannerResponseFromJson = (json: string): TrainStopPlannerResponse =>
  JSON.parse(json) as TrainStopPlannerResponse

export const trainStopPlannerResponseToJson = (value: TrainStopPlannerResponse): string =>
  JSON.stringify(value)
